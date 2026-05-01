package com.eren.social_media_analysis;

import com.eren.social_media_analysis.repository.search.SocialMediaPostRepository;
import com.eren.social_media_analysis.repository.search.TrendRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class SocialMediaAnalysisApplicationTests {

	@MockitoBean
	private SocialMediaPostRepository socialMediaPostRepository;

	@MockitoBean
	private TrendRepository trendRepository;

	@Test
	void contextLoads() {
	}

}
