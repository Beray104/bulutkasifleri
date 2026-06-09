package com.eren.social_media_analysis.service;

import com.eren.social_media_analysis.config.GroqProperties;
import com.eren.social_media_analysis.domain.search.SentimentLabel;
import com.eren.social_media_analysis.dto.SentimentAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GroqSentimentServiceTest {

	private GroqProperties properties;
	private MockRestServiceServer server;
	private GroqSentimentService service;

	@BeforeEach
	void setUp() {
		properties = new GroqProperties();
		properties.setApiKey("test-api-key");
		properties.setBaseUrl("https://api.groq.test/openai/v1");
		properties.setModel("llama-3.3-70b-versatile");

		RestClient.Builder builder = RestClient.builder()
				.baseUrl(properties.getBaseUrl());
		server = MockRestServiceServer.bindTo(builder).build();
		service = new GroqSentimentService(builder.build(), new ObjectMapper(), properties);
	}

	@Test
	void analyzesSentimentUsingGroqJsonResponse() {
		server.expect(once(), requestTo("https://api.groq.test/openai/v1/chat/completions"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("Authorization", "Bearer test-api-key"))
				.andExpect(jsonPath("$.model").value("llama-3.3-70b-versatile"))
				.andExpect(jsonPath("$.response_format.type").value("json_object"))
				.andExpect(jsonPath("$.messages[1].content").value("Urun guzel ama kargo cok kotuydu."))
				.andRespond(withSuccess(
						"""
						{
						  "choices": [
						    {
						      "message": {
						        "content": "{\\"label\\":\\"NEGATIVE\\",\\"confidence\\":0.86,\\"explanation\\":\\"Kargo deneyimi baskin olarak olumsuz.\\"}"
						      }
						    }
						  ]
						}
						""",
						MediaType.APPLICATION_JSON
				));

		SentimentAnalysisResponse response = service.analyze("  Urun guzel ama kargo cok kotuydu.  ");

		assertEquals(SentimentLabel.NEGATIVE, response.label());
		assertEquals(0.86, response.confidence());
		assertEquals("Kargo deneyimi baskin olarak olumsuz.", response.explanation());
		assertEquals("llama-3.3-70b-versatile", response.model());
		server.verify();
	}

	@Test
	void rejectsRequestWhenApiKeyIsMissing() {
		properties.setApiKey(" ");

		SentimentAnalysisUnavailableException exception = assertThrows(
				SentimentAnalysisUnavailableException.class,
				() -> service.analyze("Bu urun harika.")
		);

		assertEquals(
				"Groq API anahtari yapilandirilmamis. GROQ_API_KEY ortam degiskenini tanimlayin.",
				exception.getMessage()
		);
	}
}
