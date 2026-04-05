package backend.academy.linktracker.scrapper.repository;

import java.util.List;

public interface ChatRepository {
    void save(long chatId);

    void deleteById(long chatId);

    boolean existsById(long chatId);

    List<Long> findAllByLinkId(long linkId);
}
