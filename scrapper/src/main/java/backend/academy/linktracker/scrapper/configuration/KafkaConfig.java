package backend.academy.linktracker.scrapper.configuration;

import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic linkUpdatesTopic(KafkaProperties properties) {
        return TopicBuilder.name(properties.getTopic())
            .partitions(properties.getPartitions())
            .replicas(properties.getReplicas())
            .configs(Map.of("min.insync.replicas", "2"))
            .build();
    }
}
