package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class StackOverflowClient {
    private final RestClient restClient;

    public StackOverflowClient(
            RestClient.Builder builder,
            @Value("${app.stackoverflow.base-url:https://api.stackexchange.com/2.2}") String url) {
        this.restClient = builder.baseUrl(url).build();
    }

    public Optional<StackOverflowResponse> fetchQuestion(long questionId) {
        try {
            StackOverflowResponse response = restClient
                    .get()
                    .uri("/questions/{id}?site=stackoverflow", questionId)
                    .retrieve()
                    .body(StackOverflowResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
