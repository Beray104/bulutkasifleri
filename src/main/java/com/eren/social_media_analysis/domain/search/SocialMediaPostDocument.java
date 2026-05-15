package com.eren.social_media_analysis.domain.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Elasticsearch doküman modeli — sosyal medya postlarını indeksler.
 *
 * İndeks Adı : social_media_posts
 * Analyzer   : turkish_analyzer (ana), english_analyzer (alt alan)
 * Strateji   : Çok dilli içerik analizi için multi-field yapı kullanılır.
 */
@Document(indexName = "social_media_posts")
@Setting(settingPath = "elasticsearch/social-media-posts-settings.json")
public class SocialMediaPostDocument {

	@Id
	private String id;

	@Field(type = FieldType.Keyword)
	private String externalId;

	@Field(type = FieldType.Keyword)
	private String platform;

	@Field(type = FieldType.Keyword)
	private String authorUsername;

	/**
	 * İçerik alanı multi-field olarak indekslenir:
	 *   - Ana alan: turkish_analyzer ile Türkçe metin analizi
	 *   - .english : english_analyzer ile İngilizce metin analizi
	 *   - .keyword : Tam eşleşme ve aggregation sorguları için
	 */
	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "turkish_analyzer"),
		otherFields = {
			@InnerField(suffix = "english", type = FieldType.Text, analyzer = "english_analyzer"),
			@InnerField(suffix = "keyword", type = FieldType.Keyword)
		}
	)
	private String content;

	@Field(type = FieldType.Keyword)
	private Set<String> hashtags = new HashSet<>();

	@Field(type = FieldType.Keyword)
	private Set<String> mentions = new HashSet<>();

	@Field(type = FieldType.Keyword)
	private String language;

	@Field(type = FieldType.Double)
	private Double sentimentScore;

	@Field(type = FieldType.Keyword)
	private SentimentLabel sentimentLabel;

	@Field(type = FieldType.Integer)
	private Integer likes;

	@Field(type = FieldType.Integer)
	private Integer shares;

	@Field(type = FieldType.Integer)
	private Integer comments;

	@Field(type = FieldType.Long)
	private Long views;

	@Field(type = FieldType.Float)
	private Float engagementRate;

	@Field(type = FieldType.Keyword)
	private String mediaType;

	@Field(type = FieldType.Boolean)
	private Boolean isRepost;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant publishedAt;

	@Field(type = FieldType.Date, format = DateFormat.date_time)
	private Instant indexedAt = Instant.now();

	// ── Getter / Setter ──────────────────────────────────────

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getExternalId() { return externalId; }
	public void setExternalId(String externalId) { this.externalId = externalId; }

	public String getPlatform() { return platform; }
	public void setPlatform(String platform) { this.platform = platform; }

	public String getAuthorUsername() { return authorUsername; }
	public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }

	public Set<String> getHashtags() { return hashtags; }
	public void setHashtags(Set<String> hashtags) { this.hashtags = hashtags; }

	public Set<String> getMentions() { return mentions; }
	public void setMentions(Set<String> mentions) { this.mentions = mentions; }

	public String getLanguage() { return language; }
	public void setLanguage(String language) { this.language = language; }

	public Double getSentimentScore() { return sentimentScore; }
	public void setSentimentScore(Double sentimentScore) { this.sentimentScore = sentimentScore; }

	public SentimentLabel getSentimentLabel() { return sentimentLabel; }
	public void setSentimentLabel(SentimentLabel sentimentLabel) { this.sentimentLabel = sentimentLabel; }

	public Integer getLikes() { return likes; }
	public void setLikes(Integer likes) { this.likes = likes; }

	public Integer getShares() { return shares; }
	public void setShares(Integer shares) { this.shares = shares; }

	public Integer getComments() { return comments; }
	public void setComments(Integer comments) { this.comments = comments; }

	public Long getViews() { return views; }
	public void setViews(Long views) { this.views = views; }

	public Float getEngagementRate() { return engagementRate; }
	public void setEngagementRate(Float engagementRate) { this.engagementRate = engagementRate; }

	public String getMediaType() { return mediaType; }
	public void setMediaType(String mediaType) { this.mediaType = mediaType; }

	public Boolean getIsRepost() { return isRepost; }
	public void setIsRepost(Boolean isRepost) { this.isRepost = isRepost; }

	public Instant getPublishedAt() { return publishedAt; }
	public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

	public Instant getIndexedAt() { return indexedAt; }
	public void setIndexedAt(Instant indexedAt) { this.indexedAt = indexedAt; }
}
