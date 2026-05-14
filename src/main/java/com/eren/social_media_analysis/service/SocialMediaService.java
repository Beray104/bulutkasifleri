package com.eren.social_media_analysis.service;

import com.eren.social_media_analysis.domain.search.SentimentLabel;
import com.eren.social_media_analysis.domain.search.SocialMediaPostDocument;
import com.eren.social_media_analysis.domain.search.TrendDocument;
import com.eren.social_media_analysis.dto.SentimentSummaryResponse;
import com.eren.social_media_analysis.dto.SocialMediaMessage;
import com.eren.social_media_analysis.dto.SocialMediaPostResponse;
import com.eren.social_media_analysis.dto.TrendResponse;
import com.eren.social_media_analysis.repository.search.SocialMediaPostRepository;
import com.eren.social_media_analysis.repository.search.TrendRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SocialMediaService {

	private final SocialMediaPostRepository socialMediaPostRepository;
	private final TrendRepository trendRepository;

	public SocialMediaService(
			SocialMediaPostRepository socialMediaPostRepository,
			TrendRepository trendRepository
	) {
		this.socialMediaPostRepository = socialMediaPostRepository;
		this.trendRepository = trendRepository;
	}

	// Kafka'dan gelen veriyi Elasticsearch dokümanına dönüştürüp saklar.
	@Transactional
	public SocialMediaPostResponse processIncomingPost(SocialMediaMessage message) {
		SocialMediaPostDocument document = new SocialMediaPostDocument();
		document.setExternalId(message.externalId());
		document.setPlatform(message.platform());
		document.setAuthorUsername(message.authorUsername());
		document.setContent(message.content());
		document.setHashtags(safeHashtags(message.hashtags()));
		document.setLanguage(message.language());
		document.setSentimentScore(message.sentimentScore());
		document.setSentimentLabel(message.sentimentLabel());
		document.setPublishedAt(message.publishedAt() == null ? Instant.now() : message.publishedAt());
		document.setIndexedAt(Instant.now());

		return toPostResponse(socialMediaPostRepository.save(document));
	}

	// Web arayüzü için en güçlü trendleri DTO olarak döndürür.
	@Transactional(readOnly = true)
	public List<TrendResponse> getTopTrends() {
		return trendRepository.findTop20ByOrderByMentionCountDesc()
				.stream()
				.map(this::toTrendResponse)
				.toList();
	}

	// Web arayüzü için duygu analizi dağılımını DTO olarak döndürür.
	@Transactional(readOnly = true)
	public List<SentimentSummaryResponse> getSentimentSummary() {
		return Arrays.stream(SentimentLabel.values())
				.map(label -> new SentimentSummaryResponse(
						label,
						socialMediaPostRepository.findBySentimentLabel(label).size()
				))
				.toList();
	}

	// Son içerikler gerektiğinde entity açmadan API DTO'su üretir.
	@Transactional(readOnly = true)
	public List<SocialMediaPostResponse> getRecentPosts() {
		return socialMediaPostRepository.findTop50ByOrderByPublishedAtDesc()
				.stream()
				.map(this::toPostResponse)
				.toList();
	}

	private Set<String> safeHashtags(Set<String> hashtags) {
		if (hashtags == null) {
			return new HashSet<>();
		}
		return new HashSet<>(hashtags);
	}

	private SocialMediaPostResponse toPostResponse(SocialMediaPostDocument document) {
		return new SocialMediaPostResponse(
				document.getId(),
				document.getExternalId(),
				document.getPlatform(),
				document.getAuthorUsername(),
				document.getContent(),
				document.getHashtags(),
				document.getLanguage(),
				document.getSentimentScore(),
				document.getSentimentLabel(),
				document.getPublishedAt()
		);
	}

	private TrendResponse toTrendResponse(TrendDocument document) {
		return new TrendResponse(
				document.getId(),
				document.getKeyword(),
				document.getPlatform(),
				document.getMentionCount(),
				document.getAverageSentimentScore(),
				document.getWindowStart(),
				document.getWindowEnd()
		);
	}
}
