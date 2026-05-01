package com.eren.social_media_analysis.domain.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "social_media_trends")
public class TrendDocument {

	@Id
	private String id;

	@Field(type = FieldType.Keyword)
	private String keyword;

	@Field(type = FieldType.Keyword)
	private String platform;

	@Field(type = FieldType.Long)
	private Long mentionCount;

	@Field(type = FieldType.Double)
	private Double averageSentimentScore;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant windowStart;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant windowEnd;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public Long getMentionCount() {
		return mentionCount;
	}

	public void setMentionCount(Long mentionCount) {
		this.mentionCount = mentionCount;
	}

	public Double getAverageSentimentScore() {
		return averageSentimentScore;
	}

	public void setAverageSentimentScore(Double averageSentimentScore) {
		this.averageSentimentScore = averageSentimentScore;
	}

	public Instant getWindowStart() {
		return windowStart;
	}

	public void setWindowStart(Instant windowStart) {
		this.windowStart = windowStart;
	}

	public Instant getWindowEnd() {
		return windowEnd;
	}

	public void setWindowEnd(Instant windowEnd) {
		this.windowEnd = windowEnd;
	}
}
