package com.eren.social_media_analysis.kafka;

import com.eren.social_media_analysis.config.KafkaConfig;
import com.eren.social_media_analysis.dto.SocialMediaMessage;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer servisi.
 *
 * Sosyal medya verilerini ilgili Kafka topic'lerine yayınlar.
 * Retry mekanizması Spring Kafka producer konfigürasyonu ile sağlanır.
 * Her gönderimde callback ile başarı/hata loglanır.
 */
@Validated
@Component
public class KafkaProducer {

	private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

	public static final String SOCIAL_MEDIA_TOPIC = KafkaConfig.TOPIC_SOCIAL_MEDIA;

	private final KafkaTemplate<String, SocialMediaMessage> kafkaTemplate;

	public KafkaProducer(KafkaTemplate<String, SocialMediaMessage> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	/**
	 * Sosyal medya verisini uygun Kafka topic'ine gönderir.
	 * Key olarak platform_externalId formatı kullanılır (partition key stratejisi).
	 */
	public void publish(@Valid SocialMediaMessage message) {
		String topic = resolveTopicForPlatform(message.platform());
		String key = buildPartitionKey(message);

		CompletableFuture<SendResult<String, SocialMediaMessage>> future =
				kafkaTemplate.send(topic, key, message);

		future.whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("[KAFKA-PRODUCER] Mesaj gönderilemedi. Topic={}, Key={}, Hata={}",
						topic, key, ex.getMessage(), ex);
			} else {
				log.info("[KAFKA-PRODUCER] Mesaj gönderildi. Topic={}, Partition={}, Offset={}",
						result.getRecordMetadata().topic(),
						result.getRecordMetadata().partition(),
						result.getRecordMetadata().offset());
			}
		});
	}

	/**
	 * Ham sosyal medya verisini raw-social-data topic'ine gönderir.
	 */
	public void publishRawData(@Valid SocialMediaMessage message) {
		String key = buildPartitionKey(message);
		CompletableFuture<SendResult<String, SocialMediaMessage>> future =
				kafkaTemplate.send(KafkaConfig.TOPIC_RAW_SOCIAL_DATA, key, message);

		future.whenComplete((result, ex) -> {
			if (ex != null) {
				log.error("[KAFKA-PRODUCER] Raw data gönderilemedi. Key={}, Hata={}",
						key, ex.getMessage());
			} else {
				log.debug("[KAFKA-PRODUCER] Raw data gönderildi. Offset={}",
						result.getRecordMetadata().offset());
			}
		});
	}

	/**
	 * Platform adına göre topic belirler.
	 * Bilinen platformlar raw-social-data topic'ine, bilinmeyenler genel topic'e yönlendirilir.
	 */
	private String resolveTopicForPlatform(String platform) {
		if (platform == null) return SOCIAL_MEDIA_TOPIC;
		return switch (platform.toLowerCase()) {
			case "twitter", "facebook", "instagram", "reddit" -> KafkaConfig.TOPIC_RAW_SOCIAL_DATA;
			default -> SOCIAL_MEDIA_TOPIC;
		};
	}

	/**
	 * Partition key oluşturur: platform_externalId
	 * Aynı posta ait event'lerin aynı partition'a düşmesini sağlar.
	 */
	private String buildPartitionKey(SocialMediaMessage message) {
		String platform = message.platform() != null ? message.platform() : "unknown";
		String extId = message.externalId() != null ? message.externalId() : String.valueOf(System.nanoTime());
		return platform + "_" + extId;
	}
}
