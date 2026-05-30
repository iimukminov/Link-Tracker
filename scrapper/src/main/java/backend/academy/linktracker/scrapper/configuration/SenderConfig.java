package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import backend.academy.linktracker.scrapper.service.sender.impl.HttpBotMessageSender;
import backend.academy.linktracker.scrapper.service.sender.impl.KafkaBotMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
public class SenderConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
    public MessageSender kafkaBotMessageSender(
            KafkaTemplate<String, Object> kafkaTemplate, KafkaProperties kafkaProperties) {

        return new KafkaBotMessageSender(kafkaTemplate, kafkaProperties.getTopic());
    }

    @Bean
    @ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "false", matchIfMissing = true)
    public MessageSender httpBotMessageSender(
            BotClient botClient,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            ObjectProvider<KafkaProperties> kafkaPropertiesProvider) {

        return new HttpBotMessageSender(botClient, outboxRepository, objectMapper, kafkaPropertiesProvider);
    }
}
