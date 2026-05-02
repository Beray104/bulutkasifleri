package com.eren.social_media_analysis.analytics;

import com.eren.social_media_analysis.config.SparkProperties;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.array_intersect;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.concat_ws;
import static org.apache.spark.sql.functions.current_timestamp;
import static org.apache.spark.sql.functions.expr;
import static org.apache.spark.sql.functions.from_json;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.lower;
import static org.apache.spark.sql.functions.regexp_extract_all;
import static org.apache.spark.sql.functions.regexp_replace;
import static org.apache.spark.sql.functions.size;
import static org.apache.spark.sql.functions.split;
import static org.apache.spark.sql.functions.to_timestamp;
import static org.apache.spark.sql.functions.when;

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
		}
		catch (TimeoutException exception) {
			throw new IllegalStateException("Spark Structured Streaming job baslatilamadi.", exception);
		}
	}

	@Override
	public void stop() {
		if (streamingQuery != null) {
			try {
				streamingQuery.stop();
			}
			catch (TimeoutException exception) {
				throw new IllegalStateException("Spark Structured Streaming job durdurulamadi.", exception);
			}
		}
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	// Kafka social-media-topic kuyruğundaki JSON mesajlarını Spark stream olarak okur.
	private Dataset<Row> readFromKafka() {
		return sparkSession.readStream()
				.format("kafka")
				.option("kafka.bootstrap.servers", properties.getKafkaBootstrapServers())
				.option("subscribe", properties.getKafkaTopic())
				.option("startingOffsets", "latest")
				.load();
	}

	// Mesaj gövdesini ayrıştırır, token üretir, hashtag çıkarır ve basit sözlük tabanlı sentiment hesaplar.
	private Dataset<Row> transform(Dataset<Row> kafkaStream) {
		StructType messageSchema = new StructType()
				.add("externalId", DataTypes.StringType)
				.add("platform", DataTypes.StringType)
				.add("authorUsername", DataTypes.StringType)
				.add("content", DataTypes.StringType)
				.add("language", DataTypes.StringType)
				.add("publishedAt", DataTypes.StringType);

		Dataset<Row> parsedMessages = kafkaStream
				.selectExpr("CAST(value AS STRING) AS json")
				.select(from_json(col("json"), messageSchema).alias("message"))
				.select("message.*");

		Column cleanText = lower(regexp_replace(col("content"), "[^\\p{L}\\p{N}#@ ]", " "));
		Column tokens = split(regexp_replace(cleanText, "#", ""), "\\s+");
		Column positiveWords = expr("array('iyi','harika','mukemmel','pozitif','basarili','sevildi','great','good','happy')");
		Column negativeWords = expr("array('kotu','berbat','negatif','basarisiz','uzgun','kizgin','bad','sad','angry')");
		Column positiveCount = size(array_intersect(tokens, positiveWords));
		Column negativeCount = size(array_intersect(tokens, negativeWords));

		return parsedMessages
				.withColumn("clean_text", cleanText)
				.withColumn("tokens", tokens)
				.withColumn("hashtags", regexp_extract_all(col("content"), lit("#[\\p{L}\\p{N}_]+"), lit(0)))
				.withColumn("positive_count", positiveCount)
				.withColumn("negative_count", negativeCount)
				.withColumn("sentiment_score", positiveCount.minus(negativeCount))
				.withColumn("sentiment_label", when(col("sentiment_score").gt(0), "POSITIVE")
						.when(col("sentiment_score").lt(0), "NEGATIVE")
						.otherwise("NEUTRAL"))
				.withColumn("trend_keywords", concat_ws(",", col("hashtags")))
				.withColumn("processed_at", current_timestamp())
				.withColumn("published_at", to_timestamp(col("publishedAt")))
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
						col("published_at"),
						col("processed_at")
				);
	}

	// Analiz edilmiş stream verisini Elasticsearch social_media_analytics indeksine yazar.
	private StreamingQuery writeToElasticsearch(Dataset<Row> analyticsStream) throws TimeoutException {
		return analyticsStream.writeStream()
				.outputMode("append")
				.format("org.elasticsearch.spark.sql")
				.option("checkpointLocation", properties.getCheckpointLocation())
				.option("es.resource", properties.getElasticsearchIndex())
				.start();
	}
}
