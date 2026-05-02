package com.eren.social_media_analysis.dto;

import java.time.Instant;

public record TrendResponse(
		String id,
		String keyword,
		String platform,
		Long mentionCount,
		Double averageSentimentScore,
		Instant windowStart,
		Instant windowEnd
) {
}
