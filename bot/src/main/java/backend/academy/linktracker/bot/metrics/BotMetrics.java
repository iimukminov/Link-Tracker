package backend.academy.linktracker.bot.metrics;

import backend.academy.linktracker.common.properties.MetricsProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class BotMetrics {

    private final MeterRegistry meterRegistry;
    private final MetricsProperties properties;
    private final Counter sentNotifications;
    private final Map<String, Counter> commandCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> telegramRequestCounters = new ConcurrentHashMap<>();
    private final Map<String, DistributionSummary> commandDurationSummaries = new ConcurrentHashMap<>();
    private final Map<String, DistributionSummary> commandHandlingDurationSummaries = new ConcurrentHashMap<>();

    public BotMetrics(MeterRegistry meterRegistry, MetricsProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
        this.sentNotifications = Counter.builder("sent_notification")
                .description("Sent Telegram notifications")
                .register(meterRegistry);
    }

    public void recordCommandRequest(String command) {
        commandCounters
                .computeIfAbsent(command, key -> Counter.builder("command_requests")
                        .description("Handled bot commands")
                        .tag("command", key)
                        .register(meterRegistry))
                .increment();
    }

    public void recordTelegramRequest(String requestType) {
        telegramRequestCounters
                .computeIfAbsent(requestType, key -> Counter.builder("telegram_requests")
                        .description("Telegram updates received by request type")
                        .tag("request_type", key)
                        .register(meterRegistry))
                .increment();
    }

    public void recordCommandDuration(String scope, String scopeType, long startedAtNanos) {
        recordDuration(commandDurationSummaries, "command_duration_ms", scope, scopeType, startedAtNanos);
    }

    public void recordCommandHandlingDuration(String command, long startedAtNanos) {
        double elapsedMs = elapsedMs(startedAtNanos);
        commandHandlingDurationSummaries
                .computeIfAbsent(command, key -> DistributionSummary.builder("command_handling_duration_ms")
                        .description("Bot command handling duration in milliseconds")
                        .baseUnit("milliseconds")
                        .tag("command", key)
                        .serviceLevelObjectives(properties.getDurationBuckets())
                        .publishPercentileHistogram()
                        .register(meterRegistry))
                .record(elapsedMs);
    }

    public void recordSentNotification() {
        sentNotifications.increment();
    }

    private void recordDuration(
            Map<String, DistributionSummary> summaries,
            String metricName,
            String scope,
            String scopeType,
            long startedAtNanos) {
        double elapsedMs = elapsedMs(startedAtNanos);
        summaries
                .computeIfAbsent(scope + ":" + scopeType, ignored -> DistributionSummary.builder(metricName)
                        .description("Bot operation duration in milliseconds")
                        .baseUnit("milliseconds")
                        .tag("scope", scope)
                        .tag("scope_type", scopeType)
                        .serviceLevelObjectives(properties.getDurationBuckets())
                        .publishPercentileHistogram()
                        .register(meterRegistry))
                .record(elapsedMs);
    }

    private static double elapsedMs(long startedAtNanos) {
        return (double) (System.nanoTime() - startedAtNanos) / TimeUnit.MILLISECONDS.toNanos(1);
    }
}
