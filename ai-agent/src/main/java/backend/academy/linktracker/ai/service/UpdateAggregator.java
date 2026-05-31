package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.enums.UpdatePriority;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UpdateAggregator {

    public LinkUpdateAvro aggregate(Long chatId, List<LinkUpdateAvro> updates) {
        StringBuilder descBuilder = new StringBuilder();
        UpdatePriority maxPriority = UpdatePriority.LOW;

        long baseId = updates.get(0).getId();
        String baseUrl = updates.get(0).getUrl().toString();

        for (int i = 0; i < updates.size(); i++) {
            LinkUpdateAvro update = updates.get(i);

            descBuilder
                    .append(i + 1)
                    .append(". ")
                    .append(update.getDescription())
                    .append("\n");

            UpdatePriority currentPriority =
                    UpdatePriority.fromString(update.getPriority().toString());
            maxPriority = UpdatePriority.max(maxPriority, currentPriority);
        }

        return LinkUpdateAvro.newBuilder()
                .setId(baseId)
                .setUrl(baseUrl)
                .setDescription(descBuilder.toString().trim())
                .setAuthor("ai-agent-grouping")
                .setTgChatIds(List.of(chatId))
                .setPriority(maxPriority.name())
                .build();
    }
}
