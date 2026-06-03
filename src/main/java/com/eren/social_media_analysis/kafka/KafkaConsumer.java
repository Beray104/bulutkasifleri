package com.eren.social_media_analysis.kafka;

import com.eren.social_media_analysis.config.KafkaConfig;
import com.eren.social_media_analysis.dto.SocialMediaMessage;
import com.eren.social_media_analysis.service.SocialMediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer servisi.
 *
 * Birden fazla topic'ten mesaj okur ve iÅŸ mantÄ±ÄŸÄ± katmanÄ±na iletir.
 * Hata durumunda mesajlar dead-letter topic'ine yÃ¶nlendirilir.
 */
@Component
public class KafkaConsumer {

	private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

	private final SocialMediaService socialMediaService;
	private final KafkaTemplate<String, SocialMediaMessage> kafkaTemplate;

	public KafkaConsumer(SocialMediaService socialMediaService,
						 KafkaTemplate<String, SocialMediaMessage> kafkaTemplate) {
		this.socialMediaService = socialMediaService;
		this.kafkaTemplate = kafkaTemplate;
	}

	/**
	 * Ana topic'ten gelen mesajlarÄ± iÅŸler.
	 * Hem social-media-topic hem raw-social-data topic'lerini dinler.
	 */
	@KafkaListener(
		topics = { KafkaConfig.TOPIC_SOCIAL_MEDIA, KafkaConfig.TOPIC_RAW_SOCIAL_DATA },
		groupId = "${spring.kafka.consumer.group-id:social-media-analysis-group}",
		concurrency = "3"
	)
	public void consume(
			@Payload SocialMediaMessage message,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
			@Header(KafkaHeaders.OFFSET) long offset
	) {
		try {
			log.info("[KAFKA-CONSUMER] Mesaj alÄ±ndÄ±. Topic={}, Partition={}, Offset={}, Platform={}",
					topic, partition, offset, message.platform());

			socialMediaService.processIncomingPost(message);

			log.debug("[KAFKA-CONSUMER] Mesaj baÅŸarÄ±yla iÅŸlendi. ExternalId={}", message.externalId());
		} catch (Exception e) {
			log.error("[KAFKA-CONSUMER] Mesaj iÅŸlenemedi, dead-letter'a yÃ¶nlendiriliyor. " +
					"Topic={}, Offset={}, Hata={}", topic, offset, e.getMessage(), e);
			sendToDeadLetter(message);
		}
	}

	/**
	 * Duygu analizi sonuÃ§larÄ± topic'ini dinler.
	 */
	@KafkaListener(
		topics = KafkaConfig.TOPIC_SENTIMENT_RESULTS,
		groupId = "sentiment-results-group",
		concurrency = "2"
	)
	public void consumeSentimentResults(
			@Payload SocialMediaMessage message,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic
	) {
		try {
			log.info("[KAFKA-CONSUMER] Sentiment sonucu alÄ±ndÄ±. Topic={}, Label={}",
					topic, message.sentimentLabel());
			socialMediaService.processIncomingPost(message);
		} catch (Exception e) {
			log.error("[KAFKA-CONSUMER] Sentiment sonucu iÅŸlenemedi. Hata={}", e.getMessage(), e);
			sendToDeadLetter(message);
		}
	}

	/**
	 * Ä°ÅŸlenemeyen mesajlarÄ± dead-letter topic'ine yÃ¶nlendirir.
	 */
	private void sendToDeadLetter(SocialMediaMessage message) {
		try {
			kafkaTemplate.send(KafkaConfig.TOPIC_DEAD_LETTER, message);
			log.warn("[KAFKA-CONSUMER] Mesaj dead-letter topic'ine gÃ¶nderildi. ExternalId={}",
					message.externalId());
		} catch (Exception dlEx) {
			log.error("[KAFKA-CONSUMER] Dead-letter topic'ine de gÃ¶nderilemedi! Hata={}",
					dlEx.getMessage());
		}
	}
}
