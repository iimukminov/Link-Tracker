package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.stackoverflow")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class StackoverflowProperties {

    @NotEmpty
    private String key;

    @NotEmpty
    private String accessToken;

    @NotEmpty
    @URL
    private String baseUrl;

    @NotNull
    @DurationMin(millis = 100)
    private Duration timeout;
}
