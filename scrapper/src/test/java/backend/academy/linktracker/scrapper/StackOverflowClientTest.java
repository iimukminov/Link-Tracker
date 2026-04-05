package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class StackOverflowClientTest {

    private static WireMockServer wireMockServer;
    private static StackOverflowClient stackOverflowClient;

    @BeforeAll
    static void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        RestClient restClient = RestClient.builder().baseUrl(wireMockServer.baseUrl()).build();
        stackOverflowClient = new StackOverflowClient(restClient);
    }

    @AfterAll
    static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Должен возвращать список ответов при успешном запросе (200 OK)")
    void fetchNewAnswers_ShouldReturnAnswers_WhenSuccessful() {
        long questionId = 123456L;
        OffsetDateTime fromDate = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        long fromDateSeconds = fromDate.toEpochSecond();

        String responseBody = """
            {
              "items": [
                {
                  "answer_id": 987654,
                  "creation_date": 1704100000,
                  "owner": {
                    "display_name": "Jon Skeet"
                  },
                  "body": "Используйте CompletableFuture."
                }
              ]
            }
            """;

        stubFor(get(urlPathEqualTo("/questions/" + questionId + "/answers"))
            .withQueryParam("site", equalTo("stackoverflow"))
            .withQueryParam("fromdate", equalTo(String.valueOf(fromDateSeconds)))
            .withQueryParam("filter", equalTo("withbody"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        Optional<StackOverflowResponse> responseOpt = stackOverflowClient.fetchNewAnswers(questionId, fromDate);

        assertThat(responseOpt).isPresent();
        assertThat(responseOpt.get().items()).hasSize(1);

        StackOverflowResponse.Item item = responseOpt.get().items().getFirst();
        assertThat(item.id()).isEqualTo(987654L);
        assertThat(item.creationDate()).isEqualTo(1704100000L);
        assertThat(item.owner().displayName()).isEqualTo("Jon Skeet");
        assertThat(item.body()).isEqualTo("Используйте CompletableFuture.");
    }

    @Test
    @DisplayName("Должен возвращать Optional.empty() при ошибке API (500 Server Error)")
    void fetchNewAnswers_ShouldReturnEmpty_WhenApiFails() {
        long questionId = 123456L;
        OffsetDateTime fromDate = OffsetDateTime.now();

        stubFor(get(urlPathEqualTo("/questions/" + questionId + "/answers"))
            .willReturn(aResponse().withStatus(500)));

        Optional<StackOverflowResponse> responseOpt = stackOverflowClient.fetchNewAnswers(questionId, fromDate);

        assertThat(responseOpt).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать комментарии при успешном запросе")
    void fetchNewComments_ShouldReturnComments_WhenSuccessful() {
        long questionId = 123456L;
        OffsetDateTime fromDate = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        String responseBody = """
            {
              "items": [
                {
                  "comment_id": 111222,
                  "creation_date": 1704100500,
                  "owner": {
                    "display_name": "Alice"
                  },
                  "body": "Согласна с предыдущим оратором"
                }
              ]
            }
            """;

        stubFor(get(urlPathEqualTo("/questions/" + questionId + "/comments"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        Optional<StackOverflowResponse> responseOpt = stackOverflowClient.fetchNewComments(questionId, fromDate);

        assertThat(responseOpt).isPresent();
        StackOverflowResponse.Item item = responseOpt.get().items().getFirst();
        assertThat(item.id()).isEqualTo(111222L); // JsonAlias должен подхватить comment_id
        assertThat(item.owner().displayName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Должен возвращать заголовок вопроса")
    void fetchQuestion_ShouldReturnTitle_WhenSuccessful() {
        long questionId = 123456L;

        String responseBody = """
            {
              "items": [
                {
                  "question_id": 123456,
                  "title": "Как работает Spring Boot?"
                }
              ]
            }
            """;

        stubFor(get(urlPathEqualTo("/questions/" + questionId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        Optional<StackOverflowResponse> responseOpt = stackOverflowClient.fetchQuestion(questionId);

        assertThat(responseOpt).isPresent();
        assertThat(responseOpt.get().items().getFirst().title()).isEqualTo("Как работает Spring Boot?");
    }
}
