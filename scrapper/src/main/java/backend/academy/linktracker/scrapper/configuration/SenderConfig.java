package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import backend.academy.linktracker.scrapper.service.sender.impl.KafkaBotMessageSender;
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
            KafkaTemplate<String, LinkUpdate> kafkaTemplate,
            KafkaProperties kafkaProperties) {

        return new KafkaBotMessageSender(kafkaTemplate, kafkaProperties.getTopic());
    }
}
