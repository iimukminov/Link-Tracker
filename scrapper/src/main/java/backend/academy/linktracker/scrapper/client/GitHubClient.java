package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<GitHubResponse> fetchUpdate(String owner, String repo) {
        try {
            GitHubResponse response = restClient
                    .get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .body(GitHubResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("owner", owner)
                    .addKeyValue("repo", repo)
                    .setCause(e)
                    .log("Error fetching GitHub update");
            return Optional.empty();
        }
    }
}
