package com.eren.social_media_analysis.domain.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(indexName = "social_media_posts")
public class SocialMediaPostDocument {

	@Id
	private String id;

	@Field(type = FieldType.Keyword)
	private String externalId;

	@Field(type = FieldType.Keyword)
	private String platform;

	@Field(type = FieldType.Keyword)
	private String authorUsername;

	@Field(type = FieldType.Text, analyzer = "standard")
	private String content;

	@Field(type = FieldType.Keyword)
	private Set<String> hashtags = new HashSet<>();

	@Field(type = FieldType.Keyword)
	private String language;

	@Field(type = FieldType.Double)
	private Double sentimentScore;

	@Field(type = FieldType.Keyword)
	private SentimentLabel sentimentLabel;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant publishedAt;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant indexedAt = Instant.now();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getAuthorUsername() {
		return authorUsername;
	}

	public void setAuthorUsername(String authorUsername) {
		this.authorUsername = authorUsername;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Set<String> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<String> hashtags) {
		this.hashtags = hashtags;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Double getSentimentScore() {
		return sentimentScore;
	}

	public void setSentimentScore(Double sentimentScore) {
		this.sentimentScore = sentimentScore;
	}

	public SentimentLabel getSentimentLabel() {
		return sentimentLabel;
	}

	public void setSentimentLabel(SentimentLabel sentimentLabel) {
		this.sentimentLabel = sentimentLabel;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(Instant publishedAt) {
		this.publishedAt = publishedAt;
	}

	public Instant getIndexedAt() {
		return indexedAt;
	}

	public void setIndexedAt(Instant indexedAt) {
		this.indexedAt = indexedAt;
	}
}
