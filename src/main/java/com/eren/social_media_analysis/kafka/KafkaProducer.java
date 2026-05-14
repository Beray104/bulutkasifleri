package com.eren.social_media_analysis.kafka;

import com.eren.social_media_analysis.dto.SocialMediaMessage;
import jakarta.validation.Valid;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
public class KafkaProducer {

	public static final String SOCIAL_MEDIA_TOPIC = "social-media-topic";

	private final KafkaTemplate<String, SocialMediaMessage> kafkaTemplate;

	public KafkaProducer(KafkaTemplate<String, SocialMediaMessage> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	// Sosyal medya verisini Kafka kuyruğuna gönderir.
	public void publish(@Valid SocialMediaMessage message) {
		kafkaTemplate.send(SOCIAL_MEDIA_TOPIC, message.externalId(), message);
	}
}
