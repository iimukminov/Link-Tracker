package backend.academy.linktracker.scrapper.sender;

import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import backend.academy.linktracker.scrapper.service.sender.impl.HttpBotMessageSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {"app.use-queue=false", "app.use-outbox=false"})
public class HttpSenderIT {

    @Autowired
    private MessageSender messageSender;

    @Test
    @DisplayName("При app.use-queue=false должен загружаться HttpBotMessageSender")
    void shouldLoadHttpSenderWhenQueueIsDisabled() {
        assertTrue(messageSender instanceof HttpBotMessageSender, "Должен быть загружен HTTP клиент, а не Kafka");
    }
}
