package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.KafkaProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic dlqTopic(KafkaProperties kafkaProperties) {
        return TopicBuilder.name(kafkaProperties.getDlqTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }
}
