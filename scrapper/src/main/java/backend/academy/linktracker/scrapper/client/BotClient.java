package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class BotClient {
    private final RestClient restClient;

    public BotClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retry(name = "bot")
    @CircuitBreaker(name = "bot")
    public void sendUpdate(LinkUpdate update) {
        restClient.post().uri("/updates").body(update).retrieve().toBodilessEntity();
    }
}
