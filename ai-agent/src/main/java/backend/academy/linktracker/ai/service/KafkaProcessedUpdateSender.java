package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.metrics.AiAgentMetrics;
import backend.academy.linktracker.ai.properties.KafkaTopicProperties;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProcessedUpdateSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicProperties kafkaTopicProperties;
    private final AiAgentMetrics metrics;

    public void send(LinkUpdateAvro update) {
        String outputTopic = kafkaTopicProperties.getProcessedUpdates();
        long startedAt = System.nanoTime();

        kafkaTemplate.send(outputTopic, String.valueOf(update.getId()), update).whenComplete((result, exception) -> {
            metrics.recordDuration("kafka_send", startedAt);
            if (exception != null) {
                log.atWarn()
                        .setCause(exception)
                        .addKeyValue("updateId", update.getId())
                        .addKeyValue("topic", outputTopic)
                        .log("Failed to send processed update to Kafka");
            } else {
                log.atInfo()
                        .addKeyValue("updateId", update.getId())
                        .addKeyValue("topic", outputTopic)
                        .log("Successfully sent processed update to Kafka");
            }
        });
    }
}
