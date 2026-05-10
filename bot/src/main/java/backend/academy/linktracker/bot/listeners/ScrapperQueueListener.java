package backend.academy.linktracker.bot.listeners;

import backend.academy.linktracker.avro.LinkUpdateAvro;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.BotUpdateService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class ScrapperQueueListener {

    private final BotUpdateService botUpdateService;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(LinkUpdateAvro update) {
        log.atInfo()
                .setMessage("Received update from Kafka")
                .addKeyValue("update_id", update.getId())
                .addKeyValue("url", update.getUrl())
                .log();

        try {
            LinkUpdate dto = new LinkUpdate()
                    .id(update.getId())
                    .url(java.net.URI.create(update.getUrl()))
                    .description(update.getDescription())
                    .tgChatIds(new ArrayList<>(update.getTgChatIds()));

            botUpdateService.processUpdate(dto);
        } catch (Exception e) {
            log.atError()
                    .setMessage("Failed to process Kafka update")
                    .addKeyValue("update_id", update.getId())
                    .setCause(e)
                    .log();
            throw e;
        }
    }
}
