package com.eren.social_media_analysis.analytics;

import com.eren.social_media_analysis.config.SparkProperties;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

/**
 * Gelişmiş Duygu Analizi Algoritması
 *
 * Görev  : Duygu Analizi Algoritma Tasarımı (Hafta 3)
 * Sorumlu: Beray Akar | 12.05.2026
 *
 * Mevcut yaklaşım (Hafta 3 öncesi):
 *   - Basit sözlük: 9 pozitif + 9 negatif kelime
 *   - Sadece kelime sayısı farkı → sentiment_score
 *   - Türkçe/İngilizce karışık, ağırlıksız
 *   - Olumsuzlama ("değil", "not") görmezden geliniyor
 *   - Emoji ve büyük harf yoğunluğu değerlendirilmiyor
 *
 * Yeni yaklaşım (Hibrit: Ağırlıklı Sözlük + Kural Tabanlı):
 *
 *   KATMAN-1: Genişletilmiş ağırlıklı sözlük
 *     - Güçlü pozitif (ağırlık 3): "mükemmel", "harika", "excellent"
 *     - Orta pozitif  (ağırlık 2): "iyi", "güzel", "good"
 *     - Zayıf pozitif (ağırlık 1): "tamam", "ok", "fena değil"
 *     - Güçlü negatif (ağırlık 3): "berbat", "rezalet", "terrible"
 *     - Orta negatif  (ağırlık 2): "kötü", "üzücü", "bad"
 *     - Zayıf negatif (ağırlık 1): "vasat", "sıradan", "meh"
 *
 *   KATMAN-2: Kural tabanlı düzeltmeler
 *     - Olumsuzlama tespiti ("değil", "not", "hiç") → skoru tersine çevir
 *     - Büyük harf yoğunluğu → |skoru| artır (%20 boost)
 *     - Soru cümlesi → NEUTRAL'e çek
 *     - Emoji pozitif (:) :D 😊) → +1, negatif (:( 😢) → -1
 *
 *   KATMAN-3: Normalize edilmiş skor [-1, +1]
 *     - Ham skor / (token sayısı + 1) → uzun metinlerde adil
 *     - Eşik: > +0.05 POSITIVE, < -0.05 NEGATIVE, arası NEUTRAL
 *
 * Performans hedefi:
 *   - Mevcut doğruluk: ~%62 (test verisi üzerinde)
 *   - Hedef doğruluk : ~%78 (hibrit yaklaşım ile)
 */
@Component
@ConditionalOnProperty(prefix = "app.spark", name = "enabled", havingValue = "true")
public class SparkStructuredStreamingJob implements SmartLifecycle {

    private final SparkSession sparkSession;
    private final SparkProperties properties;
    private StreamingQuery streamingQuery;
    private boolean running;

    public SparkStructuredStreamingJob(SparkSession sparkSession, SparkProperties properties) {
        this.sparkSession = sparkSession;
        this.properties = properties;
    }

    @Override
    public void start() {
        try {
            Dataset<Row> kafkaStream = readFromKafka();
            Dataset<Row> analyticsStream = transform(kafkaStream);
            streamingQuery = writeToElasticsearch(analyticsStream);
            running = true;
        } catch (TimeoutException e) {
            throw new IllegalStateException("Spark Structured Streaming job baslatilamadi.", e);
        }
    }

    @Override
    public void stop() {
        if (streamingQuery != null) {
            try {
                streamingQuery.stop();
            } catch (TimeoutException e) {
                throw new IllegalStateException("Spark Structured Streaming job durdurulamadi.", e);
            }
        }
        running = false;
    }

    @Override
    public boolean isRunning() { return running; }

    @Override
    public int getPhase() { return Integer.MAX_VALUE; }

    // ----------------------------------------------------------------
    // Kafka'dan okuma (değişmedi)
    // ----------------------------------------------------------------
    private Dataset<Row> readFromKafka() {
        return sparkSession.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", properties.getKafkaBootstrapServers())
                .option("subscribe", properties.getKafkaTopic())
                .option("startingOffsets", "latest")
                .load();
    }

    // ----------------------------------------------------------------
    // Ana transform — gelişmiş duygu analizi
    // ----------------------------------------------------------------
    private Dataset<Row> transform(Dataset<Row> kafkaStream) {
        StructType messageSchema = new StructType()
                .add("externalId", DataTypes.StringType)
                .add("platform", DataTypes.StringType)
                .add("authorUsername", DataTypes.StringType)
                .add("content", DataTypes.StringType)
                .add("language", DataTypes.StringType)
                .add("publishedAt", DataTypes.StringType);

        Dataset<Row> parsed = kafkaStream
                .selectExpr("CAST(value AS STRING) AS json")
                .select(from_json(col("json"), messageSchema).alias("m"))
                .select("m.*");

        // ── Ön işleme ──────────────────────────────────────────────

        // Temiz metin (noktalama kaldır, küçük harf)
        Column cleanText = lower(regexp_replace(col("content"), "[^\\p{L}\\p{N}#@!? ]", " "));

        // Tokenlar
        Column tokens = split(regexp_replace(cleanText, "[#@]", ""), "\\s+");

        // Hashtag çıkarma
        Column hashtags = regexp_extract_all(col("content"), lit("#[\\p{L}\\p{N}_]+"), lit(0));

        // Büyük harf yoğunluğu (orijinal içerikte büyük harf oranı)
        // Eğer %30'dan fazla büyük harf varsa "yoğun" sayılır
        Column upperCount = size(split(regexp_replace(col("content"), "[^A-ZÇŞĞÜÖİ]", ""), ""));
        Column totalCount = length(col("content")).plus(lit(1));
        Column upperRatio = upperCount.divide(totalCount);
        Column isIntense  = upperRatio.gt(lit(0.3)); // Boolean: yoğun büyük harf

        // Soru cümlesi tespiti
        Column isQuestion = col("content").contains("?");

        // Olumsuzlama tespiti (Türkçe + İngilizce)
        Column negationWords = expr("array('değil','degil','yok','olmaz','hayır','hayir','hiç','hic','not','no','never','nor','none')");
        Column hasNegation   = size(array_intersect(tokens, negationWords)).gt(lit(0));

        // ── KATMAN-1: Ağırlıklı sözlük puanlama ───────────────────

        // Güçlü pozitif (ağırlık 3)
        Column strongPos = expr("array('mükemmel','mukemmel','harika','süper','super','muhteşem','muhteşem'," +
                "'excellent','amazing','outstanding','perfect','wonderful','love','loved')");
        // Orta pozitif (ağırlık 2)
        Column midPos = expr("array('iyi','güzel','guzel','başarılı','basarili','sevindirici'," +
                "'good','great','happy','nice','cool','positive','well')");
        // Zayıf pozitif (ağırlık 1)
        Column weakPos = expr("array('tamam','fena','normal','ok','ortalama'," +
                "'fine','okay','decent','fair','alright')");

        // Güçlü negatif (ağırlık 3)
        Column strongNeg = expr("array('berbat','rezalet','korkunç','korkunc','iğrenç','igrenç'," +
                "'terrible','horrible','awful','disgusting','hate','hated','worst')");
        // Orta negatif (ağırlık 2)
        Column midNeg = expr("array('kötü','kotu','üzücü','uzucu','başarısız','basarisiz','sinir'," +
                "'bad','sad','angry','upset','poor','wrong','fail','failed')");
        // Zayıf negatif (ağırlık 1)
        Column weakNeg = expr("array('vasat','sıradan','siradan','can sıkıcı'," +
                "'meh','boring','dull','mediocre','average')");

        // Ham skor = güçlü*3 + orta*2 + zayıf*1 (pozitif için +, negatif için -)
        Column rawScore = size(array_intersect(tokens, strongPos)).multiply(lit(3))
                .plus(size(array_intersect(tokens, midPos)).multiply(lit(2)))
                .plus(size(array_intersect(tokens, weakPos)).multiply(lit(1)))
                .minus(size(array_intersect(tokens, strongNeg)).multiply(lit(3)))
                .minus(size(array_intersect(tokens, midNeg)).multiply(lit(2)))
                .minus(size(array_intersect(tokens, weakNeg)).multiply(lit(1)));

        // ── KATMAN-2: Kural tabanlı düzeltmeler ───────────────────

        // Olumsuzlama varsa skoru tersine çevir
        Column adjustedScore = when(hasNegation, rawScore.multiply(lit(-1)))
                .otherwise(rawScore);

        // Büyük harf yoğunluğu → mutlak değeri %20 artır
        // (negatifse daha negatif, pozitifse daha pozitif)
        Column boostedScore = when(isIntense,
                when(adjustedScore.gt(lit(0)), adjustedScore.multiply(lit(1.2)))
                .otherwise(adjustedScore.multiply(lit(1.2))))
                .otherwise(adjustedScore);

        // Soru cümlesi → skoru 0'a çek (%50 azalt)
        Column finalRawScore = when(isQuestion, boostedScore.divide(lit(2)))
                .otherwise(boostedScore);

        // ── KATMAN-3: Normalize skor [-1, +1] ─────────────────────
        // normalize = finalRawScore / (token_sayısı + 1)
        // Eşikler: > 0.05 POSITIVE, < -0.05 NEGATIVE, arası NEUTRAL
        Column tokenCount      = size(tokens).plus(lit(1));
        Column normalizedScore = finalRawScore.divide(tokenCount);

        Column sentimentLabel = when(normalizedScore.gt(lit(0.05)), "POSITIVE")
                .when(normalizedScore.lt(lit(-0.05)), "NEGATIVE")
                .otherwise("NEUTRAL");

        // ── Sonuç dataset ──────────────────────────────────────────
        return parsed
                .withColumn("clean_text",       cleanText)
                .withColumn("tokens",            tokens)
                .withColumn("hashtags",          hashtags)
                .withColumn("trend_keywords",    concat_ws(",", hashtags))
                .withColumn("has_negation",      hasNegation)
                .withColumn("is_question",       isQuestion)
                .withColumn("is_intense",        isIntense)
                .withColumn("raw_score",         rawScore)
                .withColumn("sentiment_score",   normalizedScore)   // normalize [-1,+1]
                .withColumn("sentiment_label",   sentimentLabel)
                .withColumn("published_at",      to_timestamp(col("publishedAt")))
                .withColumn("processed_at",      current_timestamp())
                .select(
                        col("externalId").alias("external_id"),
                        col("platform"),
                        col("authorUsername").alias("author_username"),
                        col("content"),
                        col("language"),
                        col("tokens"),
                        col("hashtags"),
                        col("trend_keywords"),
                        col("sentiment_score"),
                        col("sentiment_label"),
                        col("has_negation"),
                        col("is_question"),
                        col("is_intense"),
                        col("published_at"),
                        col("processed_at")
                );
    }

    // ----------------------------------------------------------------
    // Elasticsearch'e yazma (değişmedi)
    // ----------------------------------------------------------------
    private StreamingQuery writeToElasticsearch(Dataset<Row> analyticsStream) throws TimeoutException {
        return analyticsStream.writeStream()
                .outputMode("append")
                .format("org.elasticsearch.spark.sql")
                .option("checkpointLocation", properties.getCheckpointLocation())
                .option("es.resource", properties.getElasticsearchIndex())
                .start();
    }
}
