package com.eren.social_media_analysis.dto;

import com.eren.social_media_analysis.domain.search.SentimentLabel;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Set;

public record SocialMediaMessage(
		String externalId,
		@NotBlank String platform,
		String authorUsername,
		@NotBlank String content,
		Set<String> hashtags,
		String language,
		Double sentimentScore,
		SentimentLabel sentimentLabel,
		Instant publishedAt
) {
}
