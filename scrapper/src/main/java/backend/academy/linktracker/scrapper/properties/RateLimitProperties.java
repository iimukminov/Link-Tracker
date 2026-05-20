package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.rate-limit")
@Validated
@Getter
@Setter
public class RateLimitProperties {
    @Positive
    private long capacity = 100;

    @NotNull
    @DurationMin(seconds = 1)
    private Duration duration = Duration.ofMinutes(1);

    @Positive
    private long cacheMaxSize = 10000;

    @NotNull
    @DurationMin(seconds = 1)
    private Duration cacheExpireAfterAccess = Duration.ofMinutes(10);
}
