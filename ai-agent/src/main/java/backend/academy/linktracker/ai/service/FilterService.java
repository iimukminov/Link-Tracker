package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.metrics.AiAgentMetrics;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilterService {

    private final AiAgentProperties properties;
    private final AiAgentMetrics metrics;

    public boolean isPass(LinkUpdateAvro update) {
        String desc = update.getDescription();
        AiAgentProperties.Filtering filterProps = properties.getFiltering();

        if (desc == null || desc.trim().length() < filterProps.getMinLength()) {
            logFiltered(update, "length is less than minimum");
            return false;
        }

        if (update.getAuthor() != null
                && filterProps.getExcludedAuthors().contains(update.getAuthor().toString())) {
            logFiltered(update, "author is excluded");
            return false;
        }

        String lowerDesc = desc.toLowerCase();
        boolean hasStopWord =
                filterProps.getStopWords().stream().anyMatch(word -> lowerDesc.contains(word.toLowerCase()));

        if (hasStopWord) {
            logFiltered(update, "contains stop word");
            return false;
        }

        return true;
    }

    private void logFiltered(LinkUpdateAvro update, String reason) {
        metrics.recordFilteredUpdate(reason);
        log.atInfo()
                .addKeyValue("updateId", update.getId())
                .addKeyValue("author", update.getAuthor())
                .log("Update filtered out: {}", reason);
    }
}
