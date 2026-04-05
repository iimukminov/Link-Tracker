package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

@Slf4j
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<GitHubIssueResponse> fetchIssuesSince(String owner, String repo, OffsetDateTime since) {
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

            return issueResponses != null ? List.of(issueResponses[0]) : List.of();
        } catch (Exception e) {
            log.atError()
                .addKeyValue("owner", owner)
                .addKeyValue("repo", repo)
                .setCause(e)
                .log("Error fetching GitHub issues");
            return List.of();
        }
    }
}
