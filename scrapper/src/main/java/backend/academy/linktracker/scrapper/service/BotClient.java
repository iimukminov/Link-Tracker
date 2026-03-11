package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BotClient {
    private final RestClient restClient;

    public BotClient(RestClient.Builder builder, @Value("${app.bot.base-url:http://localhost:8080}") String baseUrl) {

        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public void sendUpdate(LinkUpdate update) {
        restClient.post().uri("/updates").body(update).retrieve().toBodilessEntity();
    }
}
