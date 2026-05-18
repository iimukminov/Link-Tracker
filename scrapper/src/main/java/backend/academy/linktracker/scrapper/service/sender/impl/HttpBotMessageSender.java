package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.avro.LinkUpdateAvro;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class HttpBotMessageSender implements MessageSender {

    private final BotClient botClient;
    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;
    private final ObjectProvider<KafkaProperties> kafkaPropertiesProvider;

    @Override
    public void send(LinkUpdate update) {
        try {
            botClient.sendUpdate(update);
        } catch (CallNotPermittedException e) {
            log.atWarn()
                    .addKeyValue("updateId", update.getId())
                    .log("Circuit Breaker is OPEN for BotClient. Routing update to Fallback (Kafka)...");
            fallbackSend(update);
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("updateId", update.getId())
                    .setCause(e)
                    .log("HttpBotMessageSender failed after retries. Routing update to Fallback (Kafka)...");
            fallbackSend(update);
        }
    }

    private void fallbackSend(LinkUpdate update) {
        KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        KafkaProperties kafkaProperties = kafkaPropertiesProvider.getIfAvailable();

        if (kafkaTemplate != null && kafkaProperties != null) {
            try {
                LinkUpdateAvro avroMessage = LinkUpdateAvro.newBuilder()
                        .setId(update.getId())
                        .setUrl(update.getUrl().toString())
                        .setDescription(update.getDescription())
                        .setTgChatIds(update.getTgChatIds())
                        .build();

                kafkaTemplate.send(kafkaProperties.getTopic(), String.valueOf(update.getId()), avroMessage);

                log.atInfo()
                        .addKeyValue("updateId", update.getId())
                        .log("Successfully sent update via Fallback transport (Kafka).");
            } catch (Exception kafkaEx) {
                log.atError()
                        .addKeyValue("updateId", update.getId())
                        .setCause(kafkaEx)
                        .log("Critical failure: Fallback transport (Kafka) also failed!");
            }
        } else {
            log.atError()
                    .addKeyValue("updateId", update.getId())
                    .log("Fallback KafkaTemplate is bean-unavailable. Update is lost.");
        }
    }
}
