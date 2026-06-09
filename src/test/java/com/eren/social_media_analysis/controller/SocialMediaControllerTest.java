package com.eren.social_media_analysis.controller;

import com.eren.social_media_analysis.config.GlobalExceptionHandler;
import com.eren.social_media_analysis.domain.search.SentimentLabel;
import com.eren.social_media_analysis.dto.SentimentAnalysisResponse;
import com.eren.social_media_analysis.kafka.KafkaProducer;
import com.eren.social_media_analysis.service.GroqSentimentService;
import com.eren.social_media_analysis.service.SocialMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SocialMediaControllerTest {

	private GroqSentimentService groqSentimentService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		SocialMediaService socialMediaService = mock(SocialMediaService.class);
		KafkaProducer kafkaProducer = mock(KafkaProducer.class);
		groqSentimentService = mock(GroqSentimentService.class);

		SocialMediaController controller = new SocialMediaController(
				socialMediaService,
				kafkaProducer,
				groqSentimentService
		);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void returnsGroqSentimentAnalysis() throws Exception {
		when(groqSentimentService.analyze("Bu urun harika."))
				.thenReturn(new SentimentAnalysisResponse(
						SentimentLabel.POSITIVE,
						0.97,
						"Metin belirgin bicimde olumlu.",
						"llama-3.3-70b-versatile"
				));

		mockMvc.perform(post("/api/v1/sentiment-analysis")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"text\":\"Bu urun harika.\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.label").value("POSITIVE"))
				.andExpect(jsonPath("$.confidence").value(0.97))
				.andExpect(jsonPath("$.model").value("llama-3.3-70b-versatile"));

		verify(groqSentimentService).analyze("Bu urun harika.");
	}

	@Test
	void rejectsBlankTextBeforeCallingGroq() throws Exception {
		mockMvc.perform(post("/api/v1/sentiment-analysis")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"text\":\"   \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

		verifyNoInteractions(groqSentimentService);
	}
}
