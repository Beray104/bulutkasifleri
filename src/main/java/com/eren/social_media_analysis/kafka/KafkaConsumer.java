package com.eren.social_media_analysis.kafka;

import com.eren.social_media_analysis.dto.SocialMediaMessage;
import com.eren.social_media_analysis.service.SocialMediaService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

	private final SocialMediaService socialMediaService;

	public KafkaConsumer(SocialMediaService socialMediaService) {
		this.socialMediaService = socialMediaService;
	}

	// Kafka kuyruğundan gelen sosyal medya verisini iş mantığı katmanına aktarır.
	@KafkaListener(topics = KafkaProducer.SOCIAL_MEDIA_TOPIC, groupId = "${spring.kafka.consumer.group-id:social-media-analysis-group}")
	public void consume(SocialMediaMessage message) {
		socialMediaService.processIncomingPost(message);
	}
}
