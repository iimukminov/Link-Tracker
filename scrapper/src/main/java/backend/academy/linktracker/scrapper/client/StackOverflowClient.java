package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.metrics.ScrapperMetrics;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class StackOverflowClient {
    private final RestClient restClient;
    private final ScrapperMetrics metrics;

    public StackOverflowClient(RestClient restClient, ScrapperMetrics metrics) {
        this.restClient = restClient;
        this.metrics = metrics;
    }

    @Retry(name = "stackoverflow")
    @CircuitBreaker(name = "stackoverflow")
    public Optional<StackOverflowResponse> fetchNewAnswers(long questionId, OffsetDateTime fromDate) {
        long fromDateSeconds = fromDate.toEpochSecond();
        long startedAt = System.nanoTime();

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
        } finally {
            metrics.recordRequestDuration("external_source", "stackoverflow", startedAt);
        }
    }

    @Retry(name = "stackoverflow")
    @CircuitBreaker(name = "stackoverflow")
    public Optional<StackOverflowResponse> fetchNewComments(long questionId, OffsetDateTime fromDate) {
        long fromDateSeconds = fromDate.toEpochSecond();
        long startedAt = System.nanoTime();

        try {
            StackOverflowResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/questions/{id}/comments")
                    .queryParam("site", "stackoverflow")
                    .queryParam("fromdate", fromDateSeconds)
                    .queryParam("filter", "withbody")
                    .build(questionId))
                .retrieve()
                .body(StackOverflowResponse.class);
            return Optional.ofNullable(response);
        } finally {
            metrics.recordRequestDuration("external_source", "stackoverflow", startedAt);
        }
    }

    @Retry(name = "stackoverflow")
    @CircuitBreaker(name = "stackoverflow")
    public Optional<StackOverflowResponse> fetchQuestion(long questionId) {
        long startedAt = System.nanoTime();

        try {
            StackOverflowResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/questions/{id}")
                    .queryParam("site", "stackoverflow")
                    .build(questionId))
                .retrieve()
                .body(StackOverflowResponse.class);
            return Optional.ofNullable(response);
        } finally {
            metrics.recordRequestDuration("external_source", "stackoverflow", startedAt);
        }
    }
}
