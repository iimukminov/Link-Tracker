package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {
            "app.use-queue=true",
            "app.use-outbox=false",
            "app.kafka.topic=scrapper-bot-updates",
            "app.kafka.partitions=1",
            "app.kafka.replicas=1",
            "spring.kafka.consumer.group-id=test-bot-group",
            "spring.kafka.consumer.auto-offset-reset=earliest",
            "spring.kafka.properties.schema.registry.url=mock://test-registry"
        })
public class ScrapperKafkaBotIT {

    @Autowired
    private MessageSender messageSender;

    private CountDownLatch latch;

    private String receivedPayload;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);
        receivedPayload = null;
    }

    @KafkaListener(topics = "scrapper-bot-updates", groupId = "test-bot-group")
    public void listen(ConsumerRecord<Object, Object> record) {
        this.receivedPayload = String.valueOf(record.value());
        latch.countDown();
    }

    @Test
    @DisplayName("Интеграционный тест: Scrapper отправляет апдейт в Kafka, а 'Бот' (Consumer) его успешно читает")
    void shouldSendAndReceiveMessageViaKafka() throws InterruptedException {
        LinkUpdate update = new LinkUpdate()
                .id(777L)
                .url(URI.create("https://github.com/scrapper/bot"))
                .description("Test message via Kafka")
                .tgChatIds(List.of(10L, 20L));

        messageSender.send(update);

        boolean messageConsumed = latch.await(10, TimeUnit.SECONDS);

        assertThat(messageConsumed)
                .isTrue()
                .withFailMessage("Бот (Consumer) не получил сообщение из Kafka за 10 секунд");

        assertThat(receivedPayload).isNotNull();

        assertThat(receivedPayload).contains("777");
        assertThat(receivedPayload).contains("https://github.com/scrapper/bot");
        assertThat(receivedPayload).contains("Test message via Kafka");
    }
}
