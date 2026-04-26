package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class KafkaBotMessageSender implements MessageSender {

    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    private final String topicName;

    @Override
    public void send(LinkUpdate update) {
        log.atInfo()
            .setMessage("Sending link update to Kafka")
            .addKeyValue("kafka_topic", topicName)
            .addKeyValue("update_id", update.getId())
            .addKeyValue("link_url", update.getUrl())
            .log();

        kafkaTemplate.send(topicName, String.valueOf(update.getId()), update);
    }
}
