package backend.academy.linktracker.scrapper.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.links")
public record LinkProperties(int limit) {
    public LinkProperties {
        if (limit <= 0) {
            limit = 100;
        }
    }
}
