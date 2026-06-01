package backend.academy.linktracker.scrapper.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.metrics.ScrapperMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class MigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Mock
    ScrapperMetrics metrics;

    @MockitoBean
    private ObjectMapper objectMapper;

    @Test
    void testMigrationsAreApplied() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'chat'", Integer.class);

        assertTrue(count != null && count > 0, "Таблица 'chat' должна существовать после миграций");
    }
}
