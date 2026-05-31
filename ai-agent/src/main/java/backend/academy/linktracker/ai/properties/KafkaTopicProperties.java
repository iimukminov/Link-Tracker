package backend.academy.linktracker.ai.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka.topic")
@Getter
@Setter
public class KafkaTopicProperties {

    @NotBlank(message = "Raw updates topic must be configured")
    private String rawUpdates;

    @NotBlank(message = "Processed updates topic must be configured")
    private String processedUpdates;

    @NotNull
    @Min(1)
    private Integer partitions;

    @NotNull
    @Min(1)
    private Integer replicas;
}
