package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "SQL")
@RequiredArgsConstructor
public class LinkRepositoryJdbcAdapter implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean isLinkedToChat(long chatId, URI url) {
        String checkSql = "SELECT EXISTS(SELECT 1 FROM link_chat lc JOIN link l ON lc.link_id = l.id WHERE lc.chat_id = ? AND l.url = ?)";
        Boolean exists = jdbcTemplate.queryForObject(checkSql, Boolean.class, chatId, url.toString());
        return exists != null && exists;
    }

    @Override
    @Transactional
    public LinkData addLinkToChat(long chatId, URI url, List<String> tags) {
        String insertLinkSql = "INSERT INTO link (url) VALUES (?) ON CONFLICT (url) DO UPDATE SET url = EXCLUDED.url RETURNING id";
        Long linkId = jdbcTemplate.queryForObject(insertLinkSql, Long.class, url.toString());

        String insertLinkChatSql = "INSERT INTO link_chat (chat_id, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbcTemplate.update(insertLinkChatSql, chatId, linkId);

        if (tags != null && !tags.isEmpty()) {
            String insertTagSql = "INSERT INTO tag (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
            jdbcTemplate.batchUpdate(insertTagSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, tags.get(i));
                }
                @Override
                public int getBatchSize() { return tags.size(); }
            });

            String insertLinkTagSql = "INSERT INTO link_tag (link_id, tag_id) SELECT ?, id FROM tag WHERE name = ? ON CONFLICT DO NOTHING";
            jdbcTemplate.batchUpdate(insertLinkTagSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, linkId);
                    ps.setString(2, tags.get(i));
                }
                @Override
                public int getBatchSize() { return tags.size(); }
            });
        }

        return new LinkData(linkId, url, OffsetDateTime.now(), tags, List.of());
    }

    @Override
    @Transactional
    public void removeLinkFromChat(long chatId, URI url) {
        String findLinkIdSql = "SELECT id FROM link WHERE url = ?";
        List<Long> linkIds = jdbcTemplate.query(findLinkIdSql, (rs, rowNum) -> rs.getLong("id"), url.toString());

        if (!linkIds.isEmpty()) {
            String deleteSql = "DELETE FROM link_chat WHERE chat_id = ? AND link_id = ?";
            jdbcTemplate.update(deleteSql, chatId, linkIds.get(0));
        }
    }

    @Override
    public List<LinkData> findAllByChatId(long chatId, int limit, int offset) {
        String sql = """
        SELECT l.id, l.url, l.last_update, array_agg(t.name) as tags
        FROM link l
        JOIN link_chat lc ON l.id = lc.link_id
        LEFT JOIN link_tag lt ON l.id = lt.link_id
        LEFT JOIN tag t ON lt.tag_id = t.id
        WHERE lc.chat_id = ?
        GROUP BY l.id, l.url, l.last_update
        LIMIT ? OFFSET ?
    """;
        return jdbcTemplate.query(sql, new LinkDataRowMapper(), chatId, limit, offset);
    }

    @Override
    public List<LinkData> findLinksToUpdate(OffsetDateTime olderThan, int limit) {
        String sql = """
            SELECT l.id, l.url, l.last_update, array_agg(t.name) as tags
            FROM link l
            LEFT JOIN link_tag lt ON l.id = lt.link_id
            LEFT JOIN tag t ON lt.tag_id = t.id
            WHERE l.last_update < ?
            GROUP BY l.id, l.url, l.last_update
            LIMIT ?
        """;
        return jdbcTemplate.query(sql, new LinkDataRowMapper(), olderThan, limit);
    }

    @Override
    public void updateLastUpdateTime(long linkId, OffsetDateTime lastUpdate) {
        String sql = "UPDATE link SET last_update = ? WHERE id = ?";
        jdbcTemplate.update(sql, lastUpdate, linkId);
    }

    private static class LinkDataRowMapper implements RowMapper<LinkData> {
        @Override
        public LinkData mapRow(ResultSet rs, int rowNum) throws SQLException {
            java.sql.Array tagsArray = rs.getArray("tags");
            List<String> tags = List.of();
            if (tagsArray != null) {
                String[] strArray = (String[]) tagsArray.getArray();
                if (strArray.length > 0 && strArray[0] != null) {
                    tags = Arrays.asList(strArray);
                }
            }

            return new LinkData(
                rs.getLong("id"),
                URI.create(rs.getString("url")),
                rs.getObject("last_update", OffsetDateTime.class),
                tags,
                List.of());
        }
    }
}
