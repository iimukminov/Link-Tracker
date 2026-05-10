package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "use-outbox", havingValue = "true")
public class OutboxBotMessageSender implements MessageSender {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaProperties kafkaProperties;

    @Override
    @Transactional
    public void send(LinkUpdate update) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setPayload(objectMapper.writeValueAsString(update));
            event.setTopic(kafkaProperties.getTopic());
            outboxRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize and save to outbox", e);
        }
    }
}
