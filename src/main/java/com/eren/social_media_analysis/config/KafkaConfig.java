package com.eren.social_media_analysis.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka yapılandırma sınıfı.
 *
 * Topic tanımları, partition/replication ayarları,
 * hata yönetimi ve retry mekanizması burada yapılandırılır.
 */
@Configuration
public class KafkaConfig {

    // ── Topic Adları ────────────────────────────────────────
    public static final String TOPIC_RAW_SOCIAL_DATA        = "raw-social-data";
    public static final String TOPIC_PROCESSED_TWEETS       = "processed-tweets";
    public static final String TOPIC_SENTIMENT_RESULTS      = "sentiment-results";
    public static final String TOPIC_INTERACTIONS           = "social-media-interactions";
    public static final String TOPIC_DEAD_LETTER            = "social-media-dead-letter";
    public static final String TOPIC_SOCIAL_MEDIA           = "social-media-topic";

    // ── Topic Bean'leri ─────────────────────────────────────
    // Her topic için partition sayısı ve replication factor
    // Kafka mimari dokümanındaki tasarıma uygun şekilde yapılandırılmıştır.

    @Bean
    public NewTopic rawSocialDataTopic() {
        return TopicBuilder.name(TOPIC_RAW_SOCIAL_DATA)
                .partitions(6)
                .replicas(1)     // Local ortam; production'da 3
                .config("retention.ms", String.valueOf(7L * 24 * 60 * 60 * 1000)) // 7 gün
                .build();
    }

    @Bean
    public NewTopic processedTweetsTopic() {
        return TopicBuilder.name(TOPIC_PROCESSED_TWEETS)
                .partitions(6)
                .replicas(1)
                .config("retention.ms", String.valueOf(3L * 24 * 60 * 60 * 1000)) // 3 gün
                .build();
    }

    @Bean
    public NewTopic sentimentResultsTopic() {
        return TopicBuilder.name(TOPIC_SENTIMENT_RESULTS)
                .partitions(6)
                .replicas(1)
                .config("retention.ms", String.valueOf(7L * 24 * 60 * 60 * 1000)) // 7 gün
                .build();
    }

    @Bean
    public NewTopic interactionsTopic() {
        return TopicBuilder.name(TOPIC_INTERACTIONS)
                .partitions(6)
                .replicas(1)
                .config("retention.ms", String.valueOf(3L * 24 * 60 * 60 * 1000)) // 3 gün
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(TOPIC_DEAD_LETTER)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(14L * 24 * 60 * 60 * 1000)) // 14 gün
                .build();
    }

    @Bean
    public NewTopic socialMediaTopic() {
        return TopicBuilder.name(TOPIC_SOCIAL_MEDIA)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(3L * 24 * 60 * 60 * 1000)) // 3 gün
                .build();
    }

    // ── Consumer Hata Yönetimi ──────────────────────────────
    // Retry mekanizması: 3 deneme, 1 saniye aralıkla.
    // 3 denemeden sonra hata loglanır.
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        // 3 kez dene, 1 saniye bekle; başarısız olursa logla
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(1000L, 3L));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
