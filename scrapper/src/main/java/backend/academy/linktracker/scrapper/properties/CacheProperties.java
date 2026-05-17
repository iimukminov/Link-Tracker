package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@RequiredArgsConstructor
@Validated
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    @NotNull
    @DurationMin(seconds = 1)
    private Duration l1Ttl = Duration.ofMinutes(1);

    @NotNull
    @DurationMin(seconds = 1)
    private Duration l2Ttl = Duration.ofMinutes(10);

    @NotNull
    @Valid
    private ClientSide clientSide = new ClientSide();

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class ClientSide {
        private boolean enabled = true;

        @Positive
        private int maxSize = 10000;
    }
}
