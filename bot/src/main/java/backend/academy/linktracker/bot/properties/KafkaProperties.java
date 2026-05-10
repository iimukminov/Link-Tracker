package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class KafkaProperties {

    @NotEmpty
    private String topic;

    @NotEmpty
    private String dlqTopic;

    @Positive
    private int partitions;

    @Positive
    private int replicas;

    private Retry retry = new Retry();

    @Getter
    @Setter
    public static class Retry {
        @Min(1)
        private int maxAttempts = 3;

        @Min(100)
        private long backoffIntervalMs = 1000;
    }
}
