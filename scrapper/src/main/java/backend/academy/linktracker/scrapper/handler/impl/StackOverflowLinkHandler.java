package backend.academy.linktracker.scrapper.handler.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.handler.AbstractLinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.service.LinkUpdateDbService;
import backend.academy.linktracker.scrapper.service.UpdateMessageFormatter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowLinkHandler extends AbstractLinkHandler {

    private final StackOverflowClient stackOverflowClient;
    private final UpdateMessageFormatter messageFormatter;

    public StackOverflowLinkHandler(
            StackOverflowClient stackOverflowClient,
            UpdateMessageFormatter messageFormatter,
            LinkUpdateDbService linkUpdateDbService) {
        super(linkUpdateDbService);
        this.stackOverflowClient = stackOverflowClient;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("stackoverflow.com");
    }

    @Override
    protected List<LinkUpdate> fetchUpdates(List<Long> chatIds, LinkData linkData) {
        Long questionId = extractQuestionId(linkData.getUrl().getPath());
        if (questionId == null) return List.of();

        String title = stackOverflowClient
                .fetchQuestion(questionId)
                .map(resp -> resp.items().getFirst().title())
                .orElse("Без заголовка");

        List<LinkUpdate> updates = new ArrayList<>();
        OffsetDateTime maxUpdate = linkData.getLastUpdate();

        maxUpdate = processItems(
                updates,
                chatIds,
                linkData,
                maxUpdate,
                title,
                "Ответ",
                stackOverflowClient.fetchNewAnswers(questionId, linkData.getLastUpdate()));

        maxUpdate = processItems(
                updates,
                chatIds,
                linkData,
                maxUpdate,
                title,
                "Комментарий",
                stackOverflowClient.fetchNewComments(questionId, linkData.getLastUpdate()));

        linkData.setLastUpdate(maxUpdate);
        return updates;
    }

    private OffsetDateTime processItems(
            List<LinkUpdate> updates,
            List<Long> chatIds,
            LinkData linkData,
            OffsetDateTime currentMax,
            String title,
            String type,
            Optional<StackOverflowResponse> responseOpt) {

        OffsetDateTime lastUpdate = linkData.getLastUpdate();
        OffsetDateTime newMax = currentMax;

        List<StackOverflowResponse.Item> items =
                responseOpt.map(StackOverflowResponse::items).orElse(List.of());

        for (StackOverflowResponse.Item item : items) {
            Long seconds = item.lastActivityDate() != null ? item.lastActivityDate() : item.creationDate();
            if (seconds == null) continue;

            OffsetDateTime itemDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC);

            if (itemDate.isAfter(lastUpdate)) {
                String description = messageFormatter.formatStackOverflowUpdate(item, title, type, itemDate);
                String author = item.owner() != null ? item.owner().displayName() : null;

                updates.add(new LinkUpdate()
                        .id(linkData.getId())
                        .url(linkData.getUrl())
                        .description(description)
                        .author(author)
                        .tgChatIds(chatIds));

                if (itemDate.isAfter(newMax)) {
                    newMax = itemDate;
                }
            }
        }
        return newMax;
    }

    private Long extractQuestionId(String path) {
        if (path == null) return null;
        String[] parts = path.split("/");
        if (parts.length >= 3 && "questions".equals(parts[1])) {
            try {
                return Long.parseLong(parts[2]);
            } catch (NumberFormatException e) {
                log.atWarn().addKeyValue("id", parts[2]).log("Failed to parse StackOverflow ID");
            }
        }
        return null;
    }
}
