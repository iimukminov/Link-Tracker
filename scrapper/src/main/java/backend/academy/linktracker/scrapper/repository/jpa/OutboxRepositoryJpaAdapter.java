package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class OutboxRepositoryJpaAdapter implements OutboxRepository {
    private final OutboxJpaRepository jpaRepository;

    @Override
    public void save(OutboxEvent event) {
        jpaRepository.save(event);
    }

    @Override
    public List<OutboxEvent> findPending(int limit) {
        return jpaRepository.findByStatusOrderByCreatedAtAsc(
                OutboxEvent.OutboxStatus.PENDING, PageRequest.of(0, limit));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, OutboxEvent.OutboxStatus status) {
        jpaRepository.findById(id).ifPresent(e -> e.setStatus(status));
    }

    @Override
    public void incrementRetryCount(Long id) {
        jpaRepository.incrementRetryCount(id);
    }
}
