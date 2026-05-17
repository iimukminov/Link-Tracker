package backend.academy.linktracker.scrapper.sender;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
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
            "spring.kafka.properties.schema.registry.url=mock://test-registry",
            "spring.kafka.consumer.auto-offset-reset=earliest"
        })
public class ScrapperKafkaProducerIT {

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @Test
    @DisplayName("Интеграционный тест Продюсера: Scrapper успешно отправляет сообщение в Kafka-топик")
    void shouldSendMessageToKafkaTopic() {

        try (Consumer<String, Object> consumer = consumerFactory.createConsumer("test-producer-group", "test-client")) {

            consumer.subscribe(Collections.singletonList("scrapper-bot-updates"));

            LinkUpdate update = new LinkUpdate()
                    .id(777L)
                    .url(URI.create("https://github.com/scrapper/bot"))
                    .description("Test message via Kafka")
                    .tgChatIds(List.of(10L, 20L));

            messageSender.send(update);

            ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

            assertThat(records.count())
                    .withFailMessage("Сообщение не пришло в Kafka за 10 секунд")
                    .isGreaterThan(0);

            Object value = records.iterator().next().value();
            String receivedPayload = String.valueOf(value);

            assertThat(receivedPayload).contains("777");
            assertThat(receivedPayload).contains("https://github.com/scrapper/bot");
            assertThat(receivedPayload).contains("Test message via Kafka");
        }
    }
}
