package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

public class ScrapperClient {

    private final RestClient restClient;
    private final BotMetrics metrics;

    public ScrapperClient(RestClient restClient, BotMetrics metrics) {
        this.restClient = restClient;
        this.metrics = metrics;
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public void registerChat(long chatId) {
        long startedAt = System.nanoTime();
        try {
            restClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        } finally {
            metrics.recordCommandDuration("scrapper_sync_api", "registerChat", startedAt);
        }
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public void deleteChat(long chatId) {
        long startedAt = System.nanoTime();
        try {
            restClient.delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        } finally {
            metrics.recordCommandDuration("scrapper_sync_api", "deleteChat", startedAt);
        }
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public ListLinksResponse getLinks(long chatId) {
        long startedAt = System.nanoTime();
        try {
            return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .body(ListLinksResponse.class);
        } finally {
            metrics.recordCommandDuration("scrapper_sync_api", "getLinks", startedAt);
        }
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        long startedAt = System.nanoTime();
        try {
            return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
        } finally {
            metrics.recordCommandDuration("scrapper_sync_api", "addLink", startedAt);
        }
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public LinkResponse removeLink(long chatId, RemoveLinkRequest request) {
        long startedAt = System.nanoTime();
        try {
            return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
        } finally {
            metrics.recordCommandDuration("scrapper_sync_api", "removeLink", startedAt);
        }
    }
}
