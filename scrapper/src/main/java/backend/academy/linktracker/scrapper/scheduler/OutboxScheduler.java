package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.avro.LinkUpdateAvro;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import backend.academy.linktracker.scrapper.properties.OutboxProperties;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "use-outbox", havingValue = "true")
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxProperties outboxProperties;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Scheduled(fixedDelayString = "${app.outbox.interval}")
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepository.findPending(outboxProperties.getBatchSize());

        for (OutboxEvent event : events) {
            try {
                LinkUpdate update = objectMapper.readValue(event.getPayload(), LinkUpdate.class);

                LinkUpdateAvro avroUpdate = LinkUpdateAvro.newBuilder()
                        .setId(event.getId())
                        .setUrl(update.getUrl().toString())
                        .setDescription(update.getDescription())
                        .setTgChatIds(update.getTgChatIds())
                        .build();

                kafkaTemplate
                        .send(event.getTopic(), String.valueOf(event.getId()), avroUpdate)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                outboxRepository.updateStatus(event.getId(), OutboxEvent.OutboxStatus.SENT);
                                log.atDebug()
                                        .setMessage("Outbox message sent and deleted")
                                        .addKeyValue("eventId", event.getId())
                                        .addKeyValue("topic", event.getTopic())
                                        .log();
                            } else {
                                log.atError()
                                        .setMessage("Failed to send outbox message")
                                        .addKeyValue("eventId", event.getId())
                                        .addKeyValue("topic", event.getTopic())
                                        .setCause(ex)
                                        .log();
                            }
                        });
            } catch (Exception e) {
                log.atError()
                        .setMessage("Error processing outbox event")
                        .addKeyValue("eventId", event.getId())
                        .setCause(e)
                        .log();
            }
        }
    }
}
