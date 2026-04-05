package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.scheduler")
public class SchedulerProperties {

    private boolean enable;

    @NotNull
    private Duration interval;

    @NotNull
    private Duration forceCheckDelay;

    @Positive
    private int batchSize;

    @Positive
    private int threadsCount;
}
