package com.eren.social_media_analysis.service;

public class SentimentAnalysisUnavailableException extends RuntimeException {

	public SentimentAnalysisUnavailableException(String message) {
		super(message);
	}

	public SentimentAnalysisUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
