package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class StackOverflowClient {
    private final RestClient restClient;

    public StackOverflowClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<StackOverflowResponse> fetchNewAnswers(long questionId, OffsetDateTime fromDate) {
        long fromDateSeconds = fromDate.toEpochSecond();

        try {
            StackOverflowResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/questions/{id}/answers")
                    .queryParam("site", "stackoverflow")
                    .queryParam("fromdate", fromDateSeconds)
                    .queryParam("filter", "withbody")
                    .build(questionId))
                .retrieve()
                .body(StackOverflowResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.atError()
                .addKeyValue("questionId", questionId)
                .setCause(e)
                .log("Error fetching StackOverflow answers");
            return Optional.empty();
        }
    }

    public Optional<StackOverflowResponse> fetchNewComments(long questionId, OffsetDateTime fromDate) {
        long fromDateSeconds = fromDate.toEpochSecond();
        try {
            StackOverflowResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/questions/{id}/comments") // другой эндпоинт
                    .queryParam("site", "stackoverflow")
                    .queryParam("fromdate", fromDateSeconds)
                    .queryParam("filter", "withbody")
                    .build(questionId))
                .retrieve()
                .body(StackOverflowResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.atError().addKeyValue("questionId", questionId).setCause(e).log("Error fetching SO comments");
            return Optional.empty();
        }
    }

    public Optional<StackOverflowResponse> fetchQuestion(long questionId) {
        try {
            StackOverflowResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/questions/{id}")
                    .queryParam("site", "stackoverflow")
                    .build(questionId))
                .retrieve()
                .body(StackOverflowResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.atError().setCause(e).log("Error fetching SO question");
            return Optional.empty();
        }
    }
}
