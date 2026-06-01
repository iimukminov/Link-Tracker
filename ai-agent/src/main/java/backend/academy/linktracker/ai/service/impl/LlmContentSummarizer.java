package backend.academy.linktracker.ai.service.impl;

import backend.academy.linktracker.ai.metrics.AiAgentMetrics;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.service.TextSummarizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "ai-agent.summarization", name = "provider", havingValue = "API")
public class LlmContentSummarizer implements TextSummarizer {

    private final AiAgentProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final AiAgentMetrics metrics;

    public LlmContentSummarizer(
            AiAgentProperties properties,
            ObjectMapper objectMapper,
            @Qualifier("geminiRestClient") RestClient restClient,
            AiAgentMetrics metrics) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
        this.metrics = metrics;
    }

    @Override
    public String summarize(String text, int threshold) {
        AiAgentProperties.Api api = properties.getSummarization().getApi();

        if (api.getBaseUrl() == null || api.getToken() == null || api.getToken().isBlank()) {
            throw new IllegalStateException("LLM Summarization API is not properly configured");
        }

        long startedAt = System.nanoTime();
        try {
            Map<String, Object> requestBody = buildGeminiRequest(text);

            String response = restClient
                    .post()
                    .uri(URI.create(buildEndpointUrl(api)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String summary = parseResponse(response);
            if (summary == null || summary.isBlank()) {
                throw new IllegalStateException("LLM API returned empty result");
            }
            return summary;
        } finally {
            metrics.recordDuration("llm_api", startedAt);
        }
    }

    private Map<String, Object> buildGeminiRequest(String text) {
        return Map.of(
                "contents",
                List.of(Map.of(
                        "parts",
                        List.of(Map.of(
                                "text", properties.getSummarization().getApi().getPrompt() + "\n\n" + text)))));
    }

    private String buildEndpointUrl(AiAgentProperties.Api api) {
        String baseUrl = api.getBaseUrl().strip();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith(":generateContent")) {
            return baseUrl;
        }
        return baseUrl + "/models/" + api.getModel() + ":generateContent";
    }

    private String parseResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty() && parts.get(0).hasNonNull("text")) {
                    return parts.get(0).get("text").asText();
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse LLM response payload", e);
        }
        return null;
    }
}
