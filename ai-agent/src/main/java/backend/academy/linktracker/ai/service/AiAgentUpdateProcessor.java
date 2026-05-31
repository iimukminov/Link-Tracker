package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentUpdateProcessor {

    private final FilterService filterService;
    private final TextSummarizer textSummarizer;
    private final AiAgentProperties properties;
    private final PrioritizationService prioritizationService;

    public Optional<LinkUpdateAvro> process(LinkUpdateAvro rawUpdate) {
        if (!filterService.isPass(rawUpdate)) {
            return Optional.empty();
        }

        int threshold = properties.getSummarization().getThreshold();
        String description = rawUpdate.getDescription() != null ? rawUpdate.getDescription() : "";

        String priority = prioritizationService.determinePriority(description);
        String summarizedDescription = textSummarizer.summarize(description, threshold);

        log.atInfo()
                .addKeyValue("updateId", rawUpdate.getId())
                .addKeyValue("priority", priority)
                .log("Update processed successfully");

        LinkUpdateAvro processedUpdate = LinkUpdateAvro.newBuilder(rawUpdate)
                .setDescription(summarizedDescription)
                .setPriority(priority)
                .build();

        return Optional.of(processedUpdate);
    }
}
