package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {"app.use-queue=true", "app.kafka.topic=test-updates"})
public class KafkaSenderIT {

    @Autowired
    private MessageSender messageSender;

    @Test
    @DisplayName("Проверка успешной отправки сообщения в Kafka Testcontainer")
    void shouldSendMessageToKafkaWithoutExceptions() {
        LinkUpdate update = new LinkUpdate()
                .id(1L)
                .url(URI.create("https://github.com/test"))
                .description("Test Update")
                .tgChatIds(List.of(123L));

        assertDoesNotThrow(() -> messageSender.send(update), "Отправка сообщения в Kafka не должна вызывать ошибок");
    }
}
