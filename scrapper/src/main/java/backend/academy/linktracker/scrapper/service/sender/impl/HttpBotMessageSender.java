package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

@Slf4j
@RequiredArgsConstructor
public class HttpBotMessageSender implements MessageSender {

    private final BotClient botClient;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<KafkaProperties> kafkaPropertiesProvider;

    @Override
    public void send(LinkUpdate update) {
        try {
            botClient.sendUpdate(update);
        } catch (CallNotPermittedException e) {
            log.atWarn()
                    .addKeyValue("updateId", update.getId())
                    .log("Circuit Breaker is OPEN for BotClient. Routing update to Fallback (Outbox)...");
            fallbackSend(update);
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("updateId", update.getId())
                    .setCause(e)
                    .log("HttpBotMessageSender failed after retries. Routing update to Fallback (Outbox)...");
            fallbackSend(update);
        }
    }

    private void fallbackSend(LinkUpdate update) {
        KafkaProperties kafkaProperties = kafkaPropertiesProvider.getIfAvailable();

        if (kafkaProperties != null) {
            try {
                OutboxEvent event = new OutboxEvent();
                event.setPayload(objectMapper.writeValueAsString(update));
                event.setTopic(kafkaProperties.getTopic());

                outboxRepository.save(event);

                log.atInfo()
                        .addKeyValue("updateId", update.getId())
                        .log("Successfully saved update to Outbox for Fallback delivery.");
            } catch (Exception ex) {
                log.atError()
                        .addKeyValue("updateId", update.getId())
                        .setCause(ex)
                        .log("Critical failure: Failed to save to Outbox!");
            }
        } else {
            log.atError()
                    .addKeyValue("updateId", update.getId())
                    .log("Fallback KafkaProperties is bean-unavailable. Update is lost.");
        }
    }
}
