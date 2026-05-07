package com.eren.social_media_analysis.repository.search;

import com.eren.social_media_analysis.domain.search.SentimentLabel;
import com.eren.social_media_analysis.domain.search.SocialMediaPostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;
import java.util.List;

/**
 * Mevcut SocialMediaPostRepository'e optimizasyon metodları eklendi.
 *
 * Hafta 4 - Beray Akar
 *
 * Eklenen metodlar:
 *  - countBySentimentLabel      → getSentimentSummary() için 3 sorgu yerine count ile çözüm
 *  - findByPlatformAndPublishedAtAfter → platform + zaman filtreli post getirme
 */
public interface SocialMediaPostRepository extends ElasticsearchRepository<SocialMediaPostDocument, String> {

    // --- Mevcut metodlar (değiştirilmedi) ---

    List<SocialMediaPostDocument> findByPlatform(String platform);

    List<SocialMediaPostDocument> findBySentimentLabel(SentimentLabel sentimentLabel);

    List<SocialMediaPostDocument> findByPublishedAtBetween(Instant start, Instant end);

    List<SocialMediaPostDocument> findTop50ByOrderByPublishedAtDesc();

    // --- OPT-1: getSentimentSummary optimizasyonu ---
    // Mevcut kod: findBySentimentLabel(label).size() → tüm dokümanları çekiyor
    // Yeni kod  : countBySentimentLabel(label)       → sadece sayıyı döndürüyor
    long countBySentimentLabel(SentimentLabel sentimentLabel);

    // --- OPT-2: getRecentPosts optimizasyonu ---
    // Platform + zaman aralığı filtreli sorgu
    // Sadece ilgili platform ve son N saatin verisini getirir
    List<SocialMediaPostDocument> findByPlatformAndPublishedAtAfter(String platform, Instant publishedAt);

    // Platform + sentiment kombinasyonu (dashboard için)
    List<SocialMediaPostDocument> findByPlatformAndSentimentLabel(String platform, SentimentLabel sentimentLabel);

    // Son N saatin postları (tüm platformlar)
    List<SocialMediaPostDocument> findByPublishedAtAfterOrderByPublishedAtDesc(Instant publishedAt);
}
