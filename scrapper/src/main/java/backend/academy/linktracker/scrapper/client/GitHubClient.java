package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.metrics.ScrapperMetrics;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class GitHubClient {
    private final RestClient restClient;
    private final ScrapperMetrics metrics;

    public GitHubClient(RestClient restClient, ScrapperMetrics metrics) {
        this.restClient = restClient;
        this.metrics = metrics;
    }

    @Retry(name = "github")
    @CircuitBreaker(name = "github")
    public List<GitHubIssueResponse> fetchIssuesSince(String owner, String repo, OffsetDateTime since) {
        long startedAt = System.nanoTime();
        try {
            GitHubIssueResponse[] issueResponses = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/issues")
                            .queryParam("since", since.toString())
                            .queryParam("state", "all")
                            .build(owner, repo))
                    .retrieve()
                    .body(GitHubIssueResponse[].class);

            return issueResponses != null ? List.of(issueResponses) : List.of();
        } finally {
            metrics.recordRequestDuration("external_source", "github", startedAt);
        }
    }
}
