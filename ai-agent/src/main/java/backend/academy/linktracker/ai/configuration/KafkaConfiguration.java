package backend.academy.linktracker.ai.configuration;

import backend.academy.linktracker.ai.properties.KafkaTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {

    @Bean
    public NewTopic rawUpdatesTopic(KafkaTopicProperties properties) {
        return TopicBuilder.name(properties.getRawUpdates())
                .partitions(properties.getPartitions())
                .replicas(properties.getReplicas())
                .build();
    }

    @Bean
    public NewTopic processedUpdatesTopic(KafkaTopicProperties properties) {
        return TopicBuilder.name(properties.getProcessedUpdates())
                .partitions(properties.getPartitions())
                .replicas(properties.getReplicas())
                .build();
    }
}
