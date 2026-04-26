package backend.academy.linktracker.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.kafka.KafkaContainer;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
    "app.kafka.topic=test-updates",
    "app.kafka.dlq-topic=test-updates_dlq"
})
public class KafkaBotListenerIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaContainer kafkaContainer;

    @MockitoBean
    private TelegramSender telegramSender;

    @Test
    @DisplayName("Валидное сообщение из Kafka успешно обрабатывается")
    void shouldProcessValidMessage() {
        LinkUpdate update = new LinkUpdate()
            .id(1L)
            .url(URI.create("https://github.com/test"))
            .description("Test Update")
            .tgChatIds(List.of(123456L));

        kafkaTemplate.send("test-updates", String.valueOf(update.getId()), update);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
            verify(telegramSender).sendMessage(anyLong(), anyString())
        );
    }

    @Test
    @DisplayName("Битый JSON отправляется в Dead Letter Queue (DLQ)")
    void shouldSendInvalidMessageToDlq() throws Exception {
        String invalidJson = "NOT A JSON OBJECT";
        String dlqTopic = "test-updates_dlq";

        kafkaTemplate.send("test-updates", "invalid-key", invalidJson).get();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-dlq-group-" + System.currentTimeMillis(), "false");
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (Consumer<String, String> dlqConsumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer()) {

            TopicPartition partition = new TopicPartition(dlqTopic, 0);
            dlqConsumer.assign(List.of(partition));
            dlqConsumer.seekToBeginning(List.of(partition));

            org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(dlqConsumer, dlqTopic, Duration.ofSeconds(15));

            assertThat(record.value()).contains("NOT A JSON OBJECT");
        }
    }

    @Test
    @DisplayName("Дубликат сообщения из Kafka не должен отправляться повторно (идемпотентность)")
    void shouldNotSendDuplicateMessages() {
        LinkUpdate update = new LinkUpdate()
            .id(2L)
            .url(URI.create("https://github.com/test-duplicate"))
            .description("Duplicate Test")
            .tgChatIds(List.of(123456L));

        kafkaTemplate.send("test-updates", String.valueOf(update.getId()), update);
        kafkaTemplate.send("test-updates", String.valueOf(update.getId()), update);


        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(telegramSender, times(1)).sendMessage(anyLong(), anyString());
        });
    }
}
