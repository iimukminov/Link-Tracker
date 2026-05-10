package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "SQL")
@RequiredArgsConstructor
public class OutboxRepositoryJdbcAdapter implements OutboxRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(OutboxEvent event) {
        String sql = "INSERT INTO outbox_event (payload, topic, status) VALUES (?::jsonb, ?, ?)";
        jdbcTemplate.update(
                sql, event.getPayload(), event.getTopic(), event.getStatus().name());
    }

    @Override
    public List<OutboxEvent> findPending(int limit) {
        String sql = "SELECT * FROM outbox_event WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT ?";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    OutboxEvent event = new OutboxEvent();
                    event.setId(rs.getLong("id"));
                    event.setPayload(rs.getString("payload"));
                    event.setTopic(rs.getString("topic"));
                    event.setStatus(OutboxEvent.OutboxStatus.valueOf(rs.getString("status")));
                    return event;
                },
                limit);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM outbox_event WHERE id = ?", id);
    }

    @Override
    public void updateStatus(Long id, OutboxEvent.OutboxStatus status) {
        jdbcTemplate.update("UPDATE outbox_event SET status = ? WHERE id = ?", status.name(), id);
    }

    @Override
    public void incrementRetryCount(Long id) {
        jdbcTemplate.update(
                "UPDATE outbox_event SET retry_count = retry_count + 1, last_retry_at = CURRENT_TIMESTAMP WHERE id = ?",
                id);
    }
}
