package backend.academy.linktracker.scrapper;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest(
        properties = {
            "app.scheduler.enable=false",
            "app.use-queue=true",
            "app.use-outbox=true",
            "app.kafka.topic=scrapper-bot-updates"
        })
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("e2e")
public class EndToEndKafkaIT {

    @Autowired
    @Qualifier("scrapperContainer")
    private GenericContainer<?> scrapperContainer;

    @Autowired
    @Qualifier("botContainer")
    private GenericContainer<?> botContainer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM outbox_event WHERE topic = 'scrapper-bot-updates'");
    }

    @Test
    @DisplayName("E2E Тест: Outbox(Scrapper Container) -> Kafka -> Bot Container")
    void testContainersKafkaCommunicationViaOutbox() {

        String payloadJson = """
                {
                  "id": 555,
                  "url": "https://github.com/true-e2e/test",
                  "description": "True E2E Kafka Update Message",
                  "tgChatIds": [100500]
                }
                """;

        String topic = "scrapper-bot-updates";

        jdbcTemplate.update(
                "INSERT INTO outbox_event (topic, payload, status, retry_count) VALUES (?, CAST(? AS jsonb), 'PENDING', 0)",
                topic,
                payloadJson);

        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    String botLogs = botContainer.getLogs();

                    assertTrue(
                            botLogs.contains("True E2E Kafka Update Message") || botLogs.contains("555"),
                            "Бот-контейнер не вычитал сообщение из Кафки! Логи: " + botLogs);
                });
    }
}
