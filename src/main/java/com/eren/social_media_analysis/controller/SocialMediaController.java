package com.eren.social_media_analysis.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import com.eren.social_media_analysis.dto.SentimentAnalysisRequest;
import com.eren.social_media_analysis.dto.SentimentAnalysisResponse;
import com.eren.social_media_analysis.dto.SentimentSummaryResponse;
import com.eren.social_media_analysis.dto.SocialMediaMessage;
import com.eren.social_media_analysis.dto.SocialMediaPostResponse;
import com.eren.social_media_analysis.dto.TrendResponse;
import com.eren.social_media_analysis.kafka.KafkaProducer;
import com.eren.social_media_analysis.service.GroqSentimentService;
import com.eren.social_media_analysis.service.SocialMediaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(originPatterns = "*")
@RequestMapping("/api/v1")
public class SocialMediaController {

	private final SocialMediaService socialMediaService;
	private final KafkaProducer kafkaProducer;
	private final GroqSentimentService groqSentimentService;

	public SocialMediaController(
			SocialMediaService socialMediaService,
			KafkaProducer kafkaProducer,
			GroqSentimentService groqSentimentService
	) {
		this.socialMediaService = socialMediaService;
		this.kafkaProducer = kafkaProducer;
		this.groqSentimentService = groqSentimentService;
	}

	// Frontend trend verilerini bu endpoint Ã¼zerinden Ã§eker.
	@GetMapping("/trends")
	public List<TrendResponse> getTrends() {
		return socialMediaService.getTopTrends();
	}

	// Frontend duygu analizi daÄŸÄ±lÄ±mÄ±nÄ± bu endpoint Ã¼zerinden Ã§eker.
	@GetMapping("/sentiments")
	public List<SentimentSummaryResponse> getSentiments() {
		return socialMediaService.getSentimentSummary();
	}

	@PostMapping("/sentiment-analysis")
	public SentimentAnalysisResponse analyzeSentiment(
			@Valid @RequestBody SentimentAnalysisRequest request
	) {
		return groqSentimentService.analyze(request.text());
	}

	// Veri toplama katmanÄ±ndan gelen ham iÃ§eriÄŸi Kafka kuyruÄŸuna aktarÄ±r.
	@PostMapping("/social-media-posts")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void publishPost(@Valid @RequestBody SocialMediaMessage message) {
		kafkaProducer.publish(message);
	}

	// Son kayÄ±tlarÄ± DTO olarak sunar; entity/document nesneleri dÄ±ÅŸarÄ± aÃ§Ä±lmaz.
	@GetMapping("/social-media-posts")
	public List<SocialMediaPostResponse> getRecentPosts() {
		return socialMediaService.getRecentPosts();
	}
}


