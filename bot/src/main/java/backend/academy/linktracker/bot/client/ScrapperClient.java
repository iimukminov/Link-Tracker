package backend.academy.linktracker.bot.client;

// Импортируем контракты скраппера!
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

public class ScrapperClient {

    private final RestClient restClient;

    public ScrapperClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void registerChat(long chatId) {
        restClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    public void deleteChat(long chatId) {
        restClient.delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    public ListLinksResponse getLinks(long chatId) {
        return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .body(ListLinksResponse.class);
    }

    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }

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
