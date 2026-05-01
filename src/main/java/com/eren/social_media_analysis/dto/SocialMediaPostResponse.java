package com.eren.social_media_analysis.dto;

import com.eren.social_media_analysis.domain.search.SentimentLabel;

import java.time.Instant;
import java.util.Set;

public record SocialMediaPostResponse(
		String id,
		String externalId,
		String platform,
		String authorUsername,
		String content,
		Set<String> hashtags,
		String language,
		Double sentimentScore,
		SentimentLabel sentimentLabel,
		Instant publishedAt
) {
}
