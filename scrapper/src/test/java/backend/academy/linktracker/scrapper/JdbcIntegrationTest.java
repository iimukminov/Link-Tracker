package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.database.access-type=JDBC")
public class JdbcIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldLoadJdbcBeansAndNotJpaBeans() {
        assertNotNull(context.getBean(JdbcLinkRepository.class), "JDBC репозиторий должен быть загружен");

        assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> context.getBean(JpaLinkRepository.class),
                "JPA репозиторий НЕ должен быть загружен при access-type=JDBC");
    }
}
