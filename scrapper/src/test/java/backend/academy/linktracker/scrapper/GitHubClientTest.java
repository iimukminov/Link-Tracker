package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GitHubClientTest {

    @Test
    void fetchUpdate_onHttpError_shouldNotCrashAndReturnEmpty() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        GitHubClient client =
                new GitHubClient(builder.baseUrl("https://api.github.com").build());

        server.expect(requestTo("https://api.github.com/repos/user/repo")).andRespond(withServerError());

        Optional<GitHubResponse> response = client.fetchUpdate("user", "repo");

        assertTrue(response.isEmpty(), "Client should return empty Optional on HTTP error");
        server.verify();
    }

    @Test
    void fetchUpdate_onBadJsonBody_shouldNotCrashAndReturnEmpty() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        GitHubClient client =
                new GitHubClient(builder.baseUrl("https://api.github.com").build());

        server.expect(requestTo("https://api.github.com/repos/user/repo"))
                .andRespond(withSuccess("This is just a plain text, not JSON", MediaType.APPLICATION_JSON));

        Optional<GitHubResponse> response = client.fetchUpdate("user", "repo");

        assertTrue(response.isEmpty(), "Client should return empty Optional on invalid JSON body");
        server.verify();
    }
}
