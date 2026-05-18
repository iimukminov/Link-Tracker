package backend.academy.linktracker.bot.client;


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

    public ScrapperClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public void registerChat(long chatId) {
        restClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public void deleteChat(long chatId) {
        restClient.delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public ListLinksResponse getLinks(long chatId) {
        return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .body(ListLinksResponse.class);
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }

    @Retry(name = "scrapper")
    @CircuitBreaker(name = "scrapper")
    public LinkResponse removeLink(long chatId, RemoveLinkRequest request) {
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }
}
