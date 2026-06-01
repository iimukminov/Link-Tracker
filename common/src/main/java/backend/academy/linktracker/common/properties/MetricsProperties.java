package backend.academy.linktracker.common.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.metrics")
public class MetricsProperties {

    @NotEmpty(message = "Массив бакетов (percentiles) не может быть пустым")
    private double[] durationBuckets = {5.0, 10.0, 25.0, 50.0, 100.0, 250.0, 500.0, 1000.0, 2500.0, 5000.0, 10000.0};
}
