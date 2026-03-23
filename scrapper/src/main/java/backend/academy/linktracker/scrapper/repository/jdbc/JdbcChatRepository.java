package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.exceptions.ChatAlreadyRegisteredException;
import backend.academy.linktracker.scrapper.exceptions.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "JDBC")
@RequiredArgsConstructor
public class JdbcChatRepository implements ChatRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(long chatId) {
        if (existsById(chatId)) {
            throw new ChatAlreadyRegisteredException("Chat with ID " + chatId + " is already registered");
        }
        String sql = "INSERT INTO chat (id) VALUES (?)";
        jdbcTemplate.update(sql, chatId);
    }

    @Override
    public void deleteById(long chatId) {
        if (!existsById(chatId)) {
            throw new ChatNotFoundException("Chat with ID " + chatId + " not found");
        }

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
