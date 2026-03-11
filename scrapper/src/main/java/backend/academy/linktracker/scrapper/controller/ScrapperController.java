package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.InMemoryScrapperRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ScrapperController {

    private final InMemoryScrapperRepository repository;

    @PostMapping("/tg-chat/{id}")
    public void registerChat(@PathVariable long id) {
        repository.addChat(id);
    }

    @DeleteMapping("/tg-chat/{id}")
    public void deleteChat(@PathVariable long id) {
        repository.removeChat(id);
    }

    @GetMapping("/links")
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        List<LinkResponse> links = repository.getLinks(chatId).stream()
                .map(data -> new LinkResponse(data.getId(), data.getUrl(), data.getTags(), data.getFilters()))
                .toList();
        return new ListLinksResponse(links, links.size());
    }

    @PostMapping("/links")
    public LinkResponse addLink(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest request) {

        LinkData linkData = repository.addLink(chatId, request.link(), request.tags(), request.filters());
        return new LinkResponse(linkData.getId(), linkData.getUrl(), linkData.getTags(), linkData.getFilters());
    }

    @DeleteMapping("/links")
    public LinkResponse removeLink(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLinkRequest request) {

        repository.removeLink(chatId, request.link());
        return new LinkResponse(0L, request.link(), List.of(), List.of());
    }
}
