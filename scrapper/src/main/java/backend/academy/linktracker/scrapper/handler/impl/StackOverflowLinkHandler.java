package backend.academy.linktracker.scrapper.handler.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.service.UpdateMessageFormatter;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowLinkHandler implements LinkHandler {

    private final StackOverflowClient stackOverflowClient;
    private final MessageSender messageSender;
    private final UpdateMessageFormatter messageFormatter;

    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("stackoverflow.com");
    }

    @Override
    public void handle(List<Long> chatIds, LinkData linkData) {
        Long questionId = extractQuestionId(linkData.getUrl().getPath());
        if (questionId == null) return;

        String title = stackOverflowClient
                .fetchQuestion(questionId)
                .map(resp -> resp.items().getFirst().title())
                .orElse("Без заголовка");

        OffsetDateTime maxUpdate = linkData.getLastUpdate();

        maxUpdate = processItems(
                chatIds,
                linkData,
                maxUpdate,
                title,
                "Ответ",
                stackOverflowClient.fetchNewAnswers(questionId, linkData.getLastUpdate()));

        maxUpdate = processItems(
                chatIds,
                linkData,
                maxUpdate,
                title,
                "Комментарий",
                stackOverflowClient.fetchNewComments(questionId, linkData.getLastUpdate()));

        linkData.setLastUpdate(maxUpdate);
    }

    private OffsetDateTime processItems(
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

                messageSender.send(new LinkUpdate()
                        .id(linkData.getId())
                        .url(linkData.getUrl())
                        .description(description)
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
