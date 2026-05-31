package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.avro.LinkUpdateAvro;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {
            "app.kafka.topic.raw-updates=link.raw-updates.test",
            "app.kafka.topic.processed-updates=link.processed-updates.test",
            "app.kafka.topic.partitions=1",
            "app.kafka.topic.replicas=1",
            "spring.kafka.consumer.group-id=ai-agent-test-group",
            "spring.kafka.consumer.auto-offset-reset=earliest",
            "spring.kafka.producer.value-serializer=io.confluent.kafka.serializers.KafkaAvroSerializer",
            "spring.kafka.consumer.value-deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer",
            "spring.kafka.consumer.properties.specific.avro.reader=true",
            "spring.kafka.properties.schema.registry.url=mock://test-registry",
            "ai-agent.filtering.stop-words[0]=spam",
            "ai-agent.filtering.excluded-authors[0]=bot-user",
            "ai-agent.filtering.min-length=5",
            "ai-agent.summarization.provider=STUB",
            "ai-agent.summarization.threshold=15"
        })
public class RawUpdateConsumerIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @Test
    @DisplayName("E2E Тест AI Agent: Обработка сырого сообщения и отправка в выходной топик")
    void shouldConsumeRawUpdateAndPublishProcessedUpdate() throws Exception {

        try (Consumer<String, Object> consumer = consumerFactory.createConsumer("test-out-group", "test-client")) {
            consumer.subscribe(Collections.singletonList("link.processed-updates.test"));

            consumer.poll(Duration.ofMillis(500));

            LinkUpdateAvro rawUpdate = LinkUpdateAvro.newBuilder()
                    .setId(101L)
                    .setUrl("https://github.com/example/repo")
                    .setDescription("This is a very long update text that should be truncated")
                    .setAuthor("alice")
                    .setTgChatIds(List.of(10L, 20L))
                    .build();

            kafkaTemplate
                    .send("link.raw-updates.test", String.valueOf(rawUpdate.getId()), rawUpdate)
                    .get();

            ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

            assertThat(records.count())
                    .withFailMessage("Сообщение не пришло в выходной топик за 10 секунд")
                    .isGreaterThan(0);

            Object value = records.iterator().next().value();
            LinkUpdateAvro processed = (LinkUpdateAvro) value;

            assertThat(processed.getId()).isEqualTo(101L);
            assertThat(processed.getDescription()).isEqualTo("This is a very ...");
            assertThat(processed.getPriority()).isEqualTo("HIGH");
        }
    }

    @Test
    @DisplayName("Фильтрация: Сообщение со стоп-словом игнорируется")
    void shouldNotPublishFilteredUpdate() throws Exception {
        try (Consumer<String, Object> consumer = consumerFactory.createConsumer("test-out-group-2", "test-client-2")) {
            consumer.subscribe(Collections.singletonList("link.processed-updates.test"));

            consumer.poll(Duration.ofSeconds(1));

            LinkUpdateAvro rawUpdate = LinkUpdateAvro.newBuilder()
                    .setId(303L)
                    .setUrl("https://example.com/spam")
                    .setDescription("this update contains spam content")
                    .setAuthor("bob")
                    .setTgChatIds(List.of(40L))
                    .build();

            kafkaTemplate
                    .send("link.raw-updates.test", String.valueOf(rawUpdate.getId()), rawUpdate)
                    .get();

            ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(3));
            assertThat(records.isEmpty()).isTrue();
        }
    }

    @Test
    @DisplayName("Некорректное сообщение (битый формат) не должно приводить к падению")
    void shouldNotFailOnInvalidMessage() throws Exception {
        kafkaTemplate
                .send("link.raw-updates.test", "999", "invalid-binary-data".getBytes())
                .get();

        LinkUpdateAvro validUpdate = LinkUpdateAvro.newBuilder()
                .setId(999L)
                .setUrl("https://github.com/valid")
                .setDescription("Valid description")
                .setAuthor("alice")
                .setTgChatIds(List.of(1L))
                .build();

        kafkaTemplate
                .send("link.raw-updates.test", String.valueOf(validUpdate.getId()), validUpdate)
                .get();

        try (Consumer<String, Object> consumer =
                consumerFactory.createConsumer("test-check-group1", "test-client-check1")) {
            consumer.subscribe(Collections.singletonList("link.processed-updates.test"));
            ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

            assertThat(records.count()).isGreaterThan(0);
        }
    }
}
