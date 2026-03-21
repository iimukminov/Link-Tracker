package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.InMemoryScrapperRepository;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final InMemoryScrapperRepository repository;

    public ListLinksResponse getLinks(Long tgChatId) {
        List<LinkResponse> links = repository.getLinks(tgChatId).stream()
                .map(data -> new LinkResponse()
                        .id(data.getId())
                        .url(data.getUrl())
                        .tags(data.getTags())
                        .filters(data.getFilters()))
                .toList();

        return new ListLinksResponse().links(links).size(links.size());
    }

    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        LinkData linkData = repository.addLink(tgChatId, request.getLink(), request.getTags(), request.getFilters());
        return new LinkResponse()
                .id(linkData.getId())
                .url(linkData.getUrl())
                .tags(linkData.getTags())
                .filters(linkData.getFilters());
    }

    public LinkResponse removeLink(Long tgChatId, URI link) {
        repository.removeLink(tgChatId, link);
        return new LinkResponse().id(0L).url(link).tags(List.of()).filters(List.of());
    }
}
