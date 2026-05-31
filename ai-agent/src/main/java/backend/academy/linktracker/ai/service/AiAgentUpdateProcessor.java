package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiAgentUpdateProcessor {

    private static final String DEFAULT_PRIORITY = "HIGH";

    private final FilterService filterService;
    private final TextSummarizer textSummarizer;
    private final AiAgentProperties properties;

    public Optional<LinkUpdateAvro> process(LinkUpdateAvro rawUpdate) {
        if (!filterService.isPass(rawUpdate)) {
            return Optional.empty();
        }

        int threshold = properties.getSummarization().getThreshold();
        String description = rawUpdate.getDescription() != null ? rawUpdate.getDescription() : "";

        String summarizedDescription = textSummarizer.summarize(description, threshold);

        LinkUpdateAvro processedUpdate = LinkUpdateAvro.newBuilder(rawUpdate)
                .setDescription(summarizedDescription)
                .setPriority(DEFAULT_PRIORITY)
                .build();

        return Optional.of(processedUpdate);
    }
}
