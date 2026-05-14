package com.eren.social_media_analysis.repository.search;

import com.eren.social_media_analysis.domain.search.SentimentLabel;
import com.eren.social_media_analysis.domain.search.SocialMediaPostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;
import java.util.List;

public interface SocialMediaPostRepository extends ElasticsearchRepository<SocialMediaPostDocument, String> {

	List<SocialMediaPostDocument> findByPlatform(String platform);

	List<SocialMediaPostDocument> findBySentimentLabel(SentimentLabel sentimentLabel);

	List<SocialMediaPostDocument> findByPublishedAtBetween(Instant start, Instant end);

	List<SocialMediaPostDocument> findTop50ByOrderByPublishedAtDesc();
}
