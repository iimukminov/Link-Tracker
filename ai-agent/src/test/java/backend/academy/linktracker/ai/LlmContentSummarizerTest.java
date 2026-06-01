package backend.academy.linktracker.ai;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.ai.metrics.AiAgentMetrics;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.service.impl.LlmContentSummarizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class LlmContentSummarizerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private WireMockServer server;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(0);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldCallLlmApiAndReturnSummary() {
        server.stubFor(post(urlEqualTo("/models/gemini-1.5-flash:generateContent"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "candidates": [
                                    {
                                      "content": {
                                        "parts": [
                                          {
                                            "text": "Нейросетевое сокращение прошло успешно!"
                                          }
                                        ]
                                      }
                                    }
                                  ]
                                }
                                """)));

        AiAgentProperties properties = new AiAgentProperties();
        properties.getSummarization().getApi().setBaseUrl(server.baseUrl());
        properties.getSummarization().getApi().setModel("gemini-1.5-flash");
        properties.getSummarization().getApi().setToken("fake-token-123");
        properties.getSummarization().getApi().setPrompt("Сделай саммари:");
        properties.getSummarization().getApi().setTimeout(Duration.ofSeconds(2));

        AiAgentMetrics metrics = Mockito.mock(AiAgentMetrics.class);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getSummarization().getApi().getTimeout());
        requestFactory.setReadTimeout(properties.getSummarization().getApi().getTimeout());

        RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(
                        "x-goog-api-key", properties.getSummarization().getApi().getToken())
                .build();

        LlmContentSummarizer summarizer = new LlmContentSummarizer(properties, objectMapper, restClient, metrics);

        String summary = summarizer.summarize("Очень длинный текст из коммита...", 20);

        assertEquals("Нейросетевое сокращение прошло успешно!", summary);

        server.verify(postRequestedFor(urlEqualTo("/models/gemini-1.5-flash:generateContent"))
                .withHeader("x-goog-api-key", equalTo("fake-token-123"))
                .withRequestBody(matchingJsonPath(
                        "$.contents[0].parts[0].text", containing("Очень длинный текст из коммита..."))));
    }
}
