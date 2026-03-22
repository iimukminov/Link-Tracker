package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    @Transactional(readOnly = true)
    public ListLinksResponse getLinks(Long tgChatId) {
        List<LinkResponse> links = linkRepository.findAllByChatId(tgChatId, 100, 0).stream()
                .map(data -> new LinkResponse()
                        .id(data.getId())
                        .url(data.getUrl())
                        .tags(data.getTags() != null ? data.getTags() : List.of())
                        .filters(data.getFilters() != null ? data.getFilters() : List.of()))
                .toList();
        return new ListLinksResponse().links(links).size(links.size());
    }

    @Transactional
    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        LinkData linkData = linkRepository.addLinkToChat(
                tgChatId, request.getLink(), request.getTags() != null ? request.getTags() : List.of());

        return new LinkResponse()
                .id(linkData.getId())
                .url(linkData.getUrl())
                .tags(linkData.getTags() != null ? linkData.getTags() : List.of())
                .filters(List.of());
    }

    @Transactional
    public LinkResponse removeLink(Long tgChatId, URI link) {
        linkRepository.removeLinkFromChat(tgChatId, link);
        return new LinkResponse().id(0L).url(link).tags(List.of()).filters(List.of());
    }
}
