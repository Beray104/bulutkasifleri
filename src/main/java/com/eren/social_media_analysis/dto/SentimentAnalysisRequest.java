package com.eren.social_media_analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SentimentAnalysisRequest(
		@NotBlank(message = "Analiz edilecek metin bos olamaz.")
		@Size(max = 2000, message = "Analiz edilecek metin en fazla 2000 karakter olabilir.")
		String text
) {
}
