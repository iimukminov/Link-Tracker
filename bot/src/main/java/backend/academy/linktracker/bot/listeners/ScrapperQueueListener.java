package backend.academy.linktracker.bot.listeners;

import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.dto.LinkUpdate; // Твой DTO, который приходит из Scrapper
// import backend.academy.linktracker.bot.service.TelegramSender; // Твой сервис рассылки
import backend.academy.linktracker.bot.service.BotUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperQueueListener {


    private final BotUpdateService botUpdateService;

    @KafkaListener(
        topics = "${app.kafka.topic}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(LinkUpdate update) {
        log.atInfo()
           .setMessage("Received update from Kafka")
           .addKeyValue("update_id", update.getId())
           .addKeyValue("url", update.getUrl())
           .log();

        try {
            botUpdateService.processUpdate(update);
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
