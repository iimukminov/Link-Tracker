package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import java.util.List;

public interface OutboxRepository {
    void save(OutboxEvent event);

    List<OutboxEvent> findPending(int limit);

    void deleteById(Long id);

    void updateStatus(Long id, OutboxEvent.OutboxStatus status);
}
