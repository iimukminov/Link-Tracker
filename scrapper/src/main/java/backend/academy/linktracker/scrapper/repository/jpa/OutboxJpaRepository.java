package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxJpaRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus status, Pageable pageable);

    @Modifying
    @Query(
            value =
                    "UPDATE outbox_event SET retry_count = retry_count + 1, last_retry_at = CURRENT_TIMESTAMP WHERE id = :id",
            nativeQuery = true)
    void incrementRetryCount(@Param("id") Long id);
}
