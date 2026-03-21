package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class BotClient {
    private final RestClient restClient;

    public BotClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void sendUpdate(LinkUpdate update) {
        try {
            restClient.post().uri("/updates").body(update).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.atError().addKeyValue("updateId", update.getId()).setCause(e).log("Failed to send update to bot");
        }
    }
}
