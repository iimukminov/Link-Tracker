package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.repository.jdbc.LinkRepositoryJdbcAdapter;
import backend.academy.linktracker.scrapper.repository.jpa.LinkRepositoryJpaAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.database.access-type=SQL")
public class JdbcIT extends AbstractDatabaseIT {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldLoadJdbcBeansAndNotJpaBeans() {
        assertNotNull(context.getBean(LinkRepositoryJdbcAdapter.class), "SQL репозиторий должен быть загружен");

        assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> context.getBean(LinkRepositoryJpaAdapter.class),
                "ORM репозиторий НЕ должен быть загружен при access-type=SQL");
    }
}
