package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "SQL")
@RequiredArgsConstructor
public class TagRepositoryJdbcAdapter implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(String name) {
        String sql = "INSERT INTO tag (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        jdbcTemplate.update(sql, name);
    }

    @Override
    public void deleteByName(String name) {
        jdbcTemplate.update("DELETE FROM tag WHERE name = ?", name);
    }

    @Override
    public List<String> findAll() {
        return jdbcTemplate.queryForList("SELECT name FROM tag", String.class);
    }

    @Override
    public void rename(String oldName, String newName) {
        jdbcTemplate.update("UPDATE tag SET name = ? WHERE name = ?", newName, oldName);
    }
}
