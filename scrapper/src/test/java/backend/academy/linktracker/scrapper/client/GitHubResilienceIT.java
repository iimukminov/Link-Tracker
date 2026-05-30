package backend.academy.linktracker.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest(
        properties = {
            "app.scheduler.enable=false",
            "app.use-queue=false",
            "app.use-outbox=false",
            "resilience4j.circuitbreaker.instances.github.sliding-window-size=5",
            "resilience4j.circuitbreaker.instances.github.minimum-number-of-calls=5",
            "resilience4j.retry.instances.github.max-attempts=3",
            "resilience4j.retry.instances.github.wait-duration=100ms"
        })
@Import(TestcontainersConfiguration.class)
public class GitHubResilienceIT {

    @Autowired
    private GitHubClient gitHubClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void startWireMock() {
        wireMockServer =
                new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.github.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void reset() {
        wireMockServer.resetAll();
        circuitBreakerRegistry.circuitBreaker("github").transitionToClosedState();
    }

    @Test
    @DisplayName("TC-1.1: Превышение времени ожидания. Запрос падает по таймауту")
    void shouldFailOnTimeout() {
        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(200).withFixedDelay(15000)));

        assertThrows(Exception.class, () -> gitHubClient.fetchIssuesSince("owner", "repo", OffsetDateTime.now()));
    }

    @Test
    @DisplayName("TC-2.1: Retry на 5xx. Успешный ответ после двух ошибок")
    void shouldRetryOn5xx() {
        String retryScenario = "Retry Scenario";

        stubFor(get(anyUrl())
                .inScenario(retryScenario)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 2"));

        stubFor(get(anyUrl())
                .inScenario(retryScenario)
                .whenScenarioStateIs("Attempt 2")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 3"));

        stubFor(get(anyUrl())
                .inScenario(retryScenario)
                .whenScenarioStateIs("Attempt 3")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        assertDoesNotThrow(() -> gitHubClient.fetchIssuesSince("owner", "repo", OffsetDateTime.now()));

        verify(3, getRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("TC-2.2: Отсутствие Retry на 4xx. Должен сделать 1 попытку при 404 ошибке клиента")
    void shouldNotRetryOn4xx() {
        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(404)));

        assertThrows(
                HttpClientErrorException.class,
                () -> gitHubClient.fetchIssuesSince("owner", "repo", OffsetDateTime.now()));

        verify(1, getRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("TC-4.1: Circuit Breaker переходит в OPEN после серии ошибок")
    void shouldOpenCircuitBreakerAfterFailures() {
        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < 5; i++) {
            try {
                gitHubClient.fetchIssuesSince("owner", "repo", OffsetDateTime.now());
            } catch (Exception ignored) {
            }
        }

        assertThrows(
                CallNotPermittedException.class,
                () -> gitHubClient.fetchIssuesSince("owner", "repo", OffsetDateTime.now()));
    }

    @Test
    @DisplayName("TC-4.2: Circuit Breaker HALF-OPEN -> CLOSED при успешных пробных вызовах")
    void shouldTransitionFromHalfOpenToClosed() {
        var cb = circuitBreakerRegistry.circuitBreaker("github");

        cb.transitionToOpenState();
        cb.transitionToHalfOpenState();

        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(200)));

        for (int i = 0; i < 3; i++) {
            gitHubClient.fetchIssuesSince("owner", "repo", OffsetDateTime.now());
        }

        Assertions.assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    @Test
    @DisplayName("TC-4.3: Circuit Breaker HALF-OPEN -> OPEN при неудачных вызовах")
    void shouldTransitionFromHalfOpenToOpenOnFailures() {
        var cb = circuitBreakerRegistry.circuitBreaker("github");

        cb.transitionToOpenState();
        cb.transitionToHalfOpenState();

        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < 3; i++) {
            try {
                gitHubClient.fetchIssuesSince("owner", "repo", OffsetDateTime.now());
            } catch (Exception ignored) {
            }
        }

        Assertions.assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }
}
