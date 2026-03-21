package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class StackOverflowClient {
    private final RestClient restClient;

    public StackOverflowClient(RestClient restClient) {
        this.restClient = restClient;
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
            log.atError()
                    .addKeyValue("questionId", questionId)
                    .setCause(e)
                    .log("Error fetching StackOverflow question");
            return Optional.empty();
        }
    }
}
