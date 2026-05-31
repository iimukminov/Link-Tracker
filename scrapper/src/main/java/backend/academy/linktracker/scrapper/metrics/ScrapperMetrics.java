package backend.academy.linktracker.scrapper.metrics;

import backend.academy.linktracker.common.properties.MetricsProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class ScrapperMetrics {

    private final MeterRegistry meterRegistry;
    private final MetricsProperties properties;

    private final Map<String, Counter> apiRequestCounters = new ConcurrentHashMap<>();
    private final Map<String, DistributionSummary> durationSummaries = new ConcurrentHashMap<>();

    private final Map<String, AtomicLong> trackedLinks = new ConcurrentHashMap<>();

    public ScrapperMetrics(MeterRegistry meterRegistry, MetricsProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
    }

    public void recordApiRequest(String source) {
        apiRequestCounters.computeIfAbsent(source, key -> Counter.builder("api_requests")
            .description("Incoming Scrapper API requests")
            .tag("source", key)
            .register(meterRegistry)).increment();
    }

    public void recordRequestDuration(String scope, String scopeType, long startedAtNanos) {
        double elapsedMs = (double) (System.nanoTime() - startedAtNanos) / TimeUnit.MILLISECONDS.toNanos(1);
        durationSummaries.computeIfAbsent(scope + ":" + scopeType, ignored -> DistributionSummary.builder("request_duration_ms_total")
            .description("Scrapper operation duration in milliseconds")
            .baseUnit("milliseconds")
            .tag("scope", scope)
            .tag("scope_type", scopeType)
            .serviceLevelObjectives(properties.getDurationBuckets())
            .publishPercentileHistogram()
            .register(meterRegistry)).record(elapsedMs);
    }

    public void recordTrackedLinkCreated(URI url) {
        String domain = extractDomain(url);
        trackedLinks.computeIfAbsent(domain, key -> {
            AtomicLong count = new AtomicLong(0);
            Gauge.builder("links_on_track_total", count, AtomicLong::get)
                .description("Number of active links stored for monitoring")
                .tag("tracked_source", key)
                .register(meterRegistry);
            return count;
        }).incrementAndGet();
    }

    public void recordTrackedLinkDeleted(URI url) {
        String domain = extractDomain(url);
        AtomicLong count = trackedLinks.get(domain);
        if (count != null) {
            count.updateAndGet(v -> Math.max(0L, v - 1L));
        }
    }

    private String extractDomain(URI url) {
        if (url == null || url.getHost() == null) {
            return "unknown";
        }
        String host = url.getHost().toLowerCase();
        if (host.contains("github")) {
            return "github";
        }
        if (host.contains("stackoverflow")) {
            return "stackoverflow";
        }
        return host;
    }
}
