package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import backend.academy.linktracker.scrapper.entity.LinkEntity;
import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.exceptions.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exceptions.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exceptions.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "JPA")
@RequiredArgsConstructor
public class JpaLinkRepository implements LinkRepository {

    private final SpringDataLinkRepository linkRepository;
    private final SpringDataChatRepository chatRepository;
    private final SpringDataTagRepository tagRepository;

    @Override
    @Transactional
    public LinkData addLinkToChat(long chatId, URI url, List<String> tags) {
        ChatEntity chat =
                chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat ID: " + chatId));

        LinkEntity link = linkRepository.findByUrl(url.toString()).orElseGet(() -> {
            LinkEntity newLink = new LinkEntity();
            newLink.setUrl(url.toString());
            newLink.setLastUpdate(OffsetDateTime.now());
            return newLink;
        });

        if (chat.getLinks().contains(link)) {
            throw new LinkAlreadyTrackedException("Link: " + url);
        }

        if (tags != null) {
            for (String tagName : tags) {
                TagEntity tag = tagRepository.findByName(tagName).orElseGet(() -> {
                    TagEntity newTag = new TagEntity();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                });
                link.getTags().add(tag);
            }
        }

        chat.getLinks().add(link);
        linkRepository.save(link);
        chatRepository.save(chat);

        return mapToLinkData(link);
    }

    @Override
    @Transactional
    public void removeLinkFromChat(long chatId, URI url) {
        ChatEntity chat =
                chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat ID: " + chatId));

        LinkEntity link =
                linkRepository.findByUrl(url.toString()).orElseThrow(() -> new LinkNotFoundException("Link: " + url));

        if (!chat.getLinks().contains(link)) {
            throw new LinkNotFoundException("You are not subscribed to: " + url);
        }

        chat.getLinks().remove(link);
        chatRepository.save(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkData> findAllByChatId(long chatId, int limit, int offset) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException("Chat ID: " + chatId);
        }

        if (limit <= 0) {
            limit = 20;
        }
        if (offset < 0) {
            offset = 0;
        }

        return linkRepository.findAllByChatsId(chatId, PageRequest.of(offset / limit, limit)).stream()
                .map(this::mapToLinkData)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkData> findLinksToUpdate(OffsetDateTime olderThan, int limit) {
        return linkRepository.findByLastUpdateBefore(olderThan, PageRequest.of(0, limit)).stream()
                .map(this::mapToLinkData)
                .toList();
    }

    @Override
    @Transactional
    public void updateLastUpdateTime(long linkId, OffsetDateTime lastUpdate) {
        linkRepository.findById(linkId).ifPresent(link -> {
            link.setLastUpdate(lastUpdate);
            linkRepository.save(link);
        });
    }

    private LinkData mapToLinkData(LinkEntity entity) {
        List<String> tags = entity.getTags().stream().map(TagEntity::getName).toList();
        return new LinkData(entity.getId(), URI.create(entity.getUrl()), entity.getLastUpdate(), tags, List.of());
    }
}
