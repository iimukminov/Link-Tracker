package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import backend.academy.linktracker.scrapper.entity.LinkEntity;
import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class LinkRepositoryJpaAdapter implements LinkRepository {

    private final LinkJpaRepository linkRepository;
    private final ChatJpaRepository chatRepository;
    private final TagJpaRepository tagRepository;

    @Override
    public boolean isLinkedToChat(long chatId, URI url) {
        return linkRepository.existsByChatsIdAndUrl(chatId, url.toString());
    }

    @Override
    @Transactional
    public LinkData addLinkToChat(long chatId, URI url, List<String> tags) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();

        LinkEntity link = linkRepository.findByUrl(url.toString()).orElseGet(() -> {
            LinkEntity newLink = new LinkEntity();
            newLink.setUrl(url.toString());
            newLink.setLastUpdate(OffsetDateTime.now());
            newLink.setLastCheckAt(OffsetDateTime.now());
            return newLink;
        });

        if (tags != null && !tags.isEmpty()) {
            List<TagEntity> existingTags = tagRepository.findAllByNameIn(tags);
            List<String> existingTagNames =
                    existingTags.stream().map(TagEntity::getName).toList();

            List<TagEntity> newTags = tags.stream()
                    .filter(t -> !existingTagNames.contains(t))
                    .map(name -> {
                        TagEntity tag = new TagEntity();
                        tag.setName(name);
                        return tag;
                    })
                    .toList();

            if (!newTags.isEmpty()) {
                tagRepository.saveAll(newTags);
                existingTags = new ArrayList<>(existingTags);
                existingTags.addAll(newTags);
            }
            link.getTags().addAll(existingTags);
        }

        chat.getLinks().add(link);
        linkRepository.save(link);
        chatRepository.save(chat);
        return mapToLinkData(link);
    }

    @Override
    @Transactional
    public void removeLinkFromChat(long chatId, URI url) {
        chatRepository.findById(chatId).ifPresent(chat -> {
            linkRepository.findByUrl(url.toString()).ifPresent(link -> {
                chat.getLinks().remove(link);
                chatRepository.save(chat);
            });
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkData> findAllByChatId(long chatId, int limit, int offset) {
        int actualLimit = limit > 0 ? limit : 20;
        int actualOffset = Math.max(offset, 0);

        return linkRepository.findAllByChatsId(chatId, actualLimit, actualOffset).stream()
                .map(this::mapToLinkData)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkData> findLinksToUpdate(OffsetDateTime olderThan, int limit) {
        return linkRepository.findByLastCheckAtBefore(olderThan, PageRequest.of(0, limit)).stream()
                .map(this::mapToLinkData)
                .toList();
    }

    @Override
    @Transactional
    public void updateLastCheckTime(long linkId, OffsetDateTime lastCheck) {
        linkRepository.updateLastCheck(linkId, lastCheck);
    }

    @Override
    @Transactional
    public void updateLastUpdateTime(long linkId, OffsetDateTime lastUpdate) {
        linkRepository.updateLastUpdate(linkId, lastUpdate);
    }

    private LinkData mapToLinkData(LinkEntity entity) {
        List<String> tags = entity.getTags().stream().map(TagEntity::getName).toList();
        return new LinkData(entity.getId(), URI.create(entity.getUrl()), entity.getLastUpdate(), tags, List.of());
    }
}
