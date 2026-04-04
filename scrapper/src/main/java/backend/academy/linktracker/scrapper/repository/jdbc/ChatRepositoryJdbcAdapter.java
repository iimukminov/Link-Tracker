package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "SQL")
@RequiredArgsConstructor
public class ChatRepositoryJdbcAdapter implements ChatRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(long chatId) {
        String sql = "INSERT INTO chat (id) VALUES (?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public void deleteById(long chatId) {
        String sql = "DELETE FROM chat WHERE id = ?";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public boolean existsById(long chatId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM chat WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, chatId);
        return exists != null && exists;
    }

    @Override
    public List<Long> findAllByLinkId(long linkId) {
        String sql = "SELECT chat_id FROM link_chat WHERE link_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, linkId);
    }
}
