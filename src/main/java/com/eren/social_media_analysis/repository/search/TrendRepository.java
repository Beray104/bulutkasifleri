package com.eren.social_media_analysis.repository.search;

import com.eren.social_media_analysis.domain.search.TrendDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;
import java.util.List;

public interface TrendRepository extends ElasticsearchRepository<TrendDocument, String> {

	List<TrendDocument> findByKeyword(String keyword);

	List<TrendDocument> findByPlatformAndWindowStartGreaterThanEqual(String platform, Instant windowStart);

	List<TrendDocument> findTop20ByOrderByMentionCountDesc();
}
