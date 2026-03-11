package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient(
            RestClient.Builder builder,
            @Value("${app.github.base-url:https://api.github.com}") String url,
            GithubProperties githubProperties) {
        this.restClient = builder.baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubProperties.getToken())
                .build();
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
            return Optional.empty();
        }
    }
}
