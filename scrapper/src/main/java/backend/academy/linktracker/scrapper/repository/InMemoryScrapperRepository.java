package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.exceptions.ChatAlreadyRegisteredException;
import backend.academy.linktracker.scrapper.exceptions.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exceptions.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exceptions.LinkNotFoundException;
import backend.academy.linktracker.scrapper.model.LinkData;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryScrapperRepository {
    private final Map<Long, Set<LinkData>> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public void addChat(long chatId) {
        if (storage.putIfAbsent(chatId, ConcurrentHashMap.newKeySet()) != null) {
            throw new ChatAlreadyRegisteredException("Chat ID: " + chatId);
        }
    }

    public void removeChat(long chatId) {
        if (storage.remove(chatId) == null) {
            throw new ChatNotFoundException("Chat ID: " + chatId);
        }
    }

    public LinkData addLink(long chatId, URI url, List<String> tags, List<String> filters) {
        Set<LinkData> links = storage.get(chatId);
        if (links == null) {
            throw new ChatNotFoundException("Chat ID: " + chatId);
        }

        if (links.stream().anyMatch(l -> l.getUrl().equals(url))) {
            throw new LinkAlreadyTrackedException("Link: " + url);
        }

        LinkData newLink = new LinkData(idGenerator.getAndIncrement(), url, OffsetDateTime.now(), tags, filters);
        links.add(newLink);
        return newLink;
    }

    public void removeLink(long chatId, URI url) {
        Set<LinkData> links = storage.get(chatId);
        if (links == null) {
            throw new ChatNotFoundException("Chat ID: " + chatId);
        }

        boolean removed = links.removeIf(l -> l.getUrl().equals(url));

        if (!removed) {
            throw new LinkNotFoundException("Link: " + url);
        }
    }

    public Set<LinkData> getLinks(long chatId) {
        if (!storage.containsKey(chatId)) {
            throw new ChatNotFoundException("Chat ID: " + chatId);
        }
        return storage.getOrDefault(chatId, Set.of());
    }

    public Map<Long, Set<LinkData>> findAll() {
        return storage;
    }
}
