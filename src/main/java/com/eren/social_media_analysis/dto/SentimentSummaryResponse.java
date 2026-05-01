package com.eren.social_media_analysis.dto;

import com.eren.social_media_analysis.domain.search.SentimentLabel;

public record SentimentSummaryResponse(
		SentimentLabel label,
		long count
) {
}
