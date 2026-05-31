package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.avro.LinkUpdateAvro;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class KafkaBotMessageSender implements MessageSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topicName;

    @Override
    public void send(LinkUpdate update) {
        log.atInfo()
                .setMessage("Sending link update to Kafka")
                .addKeyValue("kafka_topic", topicName)
                .addKeyValue("update_id", update.getId())
                .addKeyValue("link_url", update.getUrl())
                .log();

        LinkUpdateAvro avroMessage = LinkUpdateAvro.newBuilder()
                .setId(update.getId())
                .setUrl(update.getUrl().toString())
                .setDescription(update.getDescription())
                .setTgChatIds(update.getTgChatIds())
                .setAuthor(update.getAuthor())
                .setPriority(update.getPriority())
                .build();

        kafkaTemplate.send(topicName, String.valueOf(update.getId()), avroMessage);
    }
}
