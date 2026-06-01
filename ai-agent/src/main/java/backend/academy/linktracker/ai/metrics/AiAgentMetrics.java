package backend.academy.linktracker.ai.metrics;

import backend.academy.linktracker.common.properties.MetricsProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class AiAgentMetrics {

    private final MeterRegistry meterRegistry;
    private final MetricsProperties properties;
    private final Map<String, DistributionSummary> durationSummaries = new ConcurrentHashMap<>();

    public AiAgentMetrics(MeterRegistry meterRegistry, MetricsProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
    }

    public void recordFilteredUpdate(String reason) {
        Counter.builder("ai_agent_filtered_updates_total")
                .description("Количество отфильтрованных обновлений")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordDuration(String scope, long startedAtNanos) {
        double elapsedMs = (double) (System.nanoTime() - startedAtNanos) / TimeUnit.MILLISECONDS.toNanos(1);
        durationSummaries
                .computeIfAbsent(scope, key -> DistributionSummary.builder("request_duration_ms_total")
                        .description("Длительность операций AI Agent в миллисекундах")
                        .baseUnit("milliseconds")
                        .tag("scope", key)
                        .serviceLevelObjectives(properties.getDurationBuckets())
                        .publishPercentileHistogram()
                        .register(meterRegistry))
                .record(elapsedMs);
    }
}
