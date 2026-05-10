package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.outbox")
public class OutboxProperties {

    @Positive
    private int interval;

    @Positive
    private int batchSize;
}
