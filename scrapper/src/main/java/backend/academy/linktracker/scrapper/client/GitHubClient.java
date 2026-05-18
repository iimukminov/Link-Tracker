package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retry(name = "github")
    @CircuitBreaker(name = "github")
    public List<GitHubIssueResponse> fetchIssuesSince(String owner, String repo, OffsetDateTime since) {
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
    }
}
