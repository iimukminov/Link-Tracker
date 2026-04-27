package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class GitHubClientTest {

    private static WireMockServer wireMockServer;
    private static GitHubClient gitHubClient;

    @BeforeAll
    static void setUp() {
        wireMockServer =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        RestClient restClient =
                RestClient.builder().baseUrl(wireMockServer.baseUrl()).build();
        gitHubClient = new GitHubClient(restClient);
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Должен возвращать список Issue, если GitHub API отвечает успешно")
    void fetchIssuesSince_ShouldReturnIssues_WhenApiIsSuccessful() {
        String owner = "test-owner";
        String repo = "test-repo";
        OffsetDateTime since = OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        String responseBody = """
            [
              {
                "id": 12345,
                "title": "Добавить тесты",
                "html_url": "https://github.com/test/test/issues/1",
                "created_at": "2023-10-10T10:00:00Z",
                "body": "Нам нужно больше тестов!",
                "user": {
                  "login": "test-user"
                }
              }
            ]
            """;

        stubFor(get(urlPathEqualTo(String.format("/repos/%s/%s/issues", owner, repo)))
                .withQueryParam("since", equalTo(since.toString()))
                .withQueryParam("state", equalTo("all"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        List<GitHubIssueResponse> issues = gitHubClient.fetchIssuesSince(owner, repo, since);

        assertThat(issues).hasSize(1);
        GitHubIssueResponse issue = issues.getFirst();
        assertThat(issue.id()).isEqualTo(12345L);
        assertThat(issue.title()).isEqualTo("Добавить тесты");
    }

    @Test
    @DisplayName("Должен пробрасывать исключение, если GitHub API вернул ошибку")
    void fetchIssuesSince_ShouldThrowException_WhenApiFails() {
        String owner = "test-owner";
        String repo = "test-repo";
        OffsetDateTime since = OffsetDateTime.now();

        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(500)));

        assertThrows(RestClientException.class, () -> gitHubClient.fetchIssuesSince(owner, repo, since));
    }
}
