package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.LinkData;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkRepository {
    LinkData addLinkToChat(long chatId, URI url, List<String> tags);

    void removeLinkFromChat(long chatId, URI url);

    List<LinkData> findAllByChatId(long chatId, int limit, int offset);

    List<LinkData> findLinksToUpdate(OffsetDateTime olderThan, int limit);

    void updateLastUpdateTime(long linkId, OffsetDateTime lastUpdate);
}
