package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.api.LinksApi;
import backend.academy.linktracker.scrapper.api.TgChatApi;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

@RestController
@RequiredArgsConstructor
public class ScrapperController implements TgChatApi, LinksApi {

    private final TgChatService tgChatService;
    private final LinkService linkService;

    @Override
    public ResponseEntity<Void> tgChatIdPost(Long id) {
        tgChatService.registerChat(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> tgChatIdDelete(Long id) {
        tgChatService.deleteChat(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ListLinksResponse> linksGet(Long tgChatId) {
        return ResponseEntity.ok(linkService.getLinks(tgChatId));
    }

    @Override
    public ResponseEntity<LinkResponse> linksPost(Long tgChatId, AddLinkRequest request) {
        return ResponseEntity.ok(linkService.addLink(tgChatId, request));
    }

    @Override
    public ResponseEntity<LinkResponse> linksDelete(Long tgChatId, RemoveLinkRequest request) {
        return ResponseEntity.ok(linkService.removeLink(tgChatId, request.getLink()));
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }
}
