package backend.academy.linktracker.scrapper.handler.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
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
    private final ScrapperMessages scrapperMessages;

    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("stackoverflow.com");
    }

    @Override
    public void handle(List<Long> chatIds, LinkData linkData) {
        String path = linkData.getUrl().getPath();
        if (path == null) {
            return;
        }

        String[] parts = path.split("/");

        if (parts.length >= 3 && parts[1].equals("questions")) {
            try {
                long questionId = Long.parseLong(parts[2]);

                String questionTitle = stackOverflowClient
                        .fetchQuestion(questionId)
                        .map(resp -> {
                            if (resp.items() != null && !resp.items().isEmpty()) {
                                return resp.items().getFirst().title();
                            }
                            return "Без заголовка";
                        })
                        .orElse("Без заголовка");

                OffsetDateTime maxUpdate = linkData.getLastUpdate();

                maxUpdate = processUpdates(
                        chatIds,
                        linkData,
                        maxUpdate,
                        stackOverflowClient.fetchNewAnswers(questionId, linkData.getLastUpdate()),
                        questionTitle);

                maxUpdate = processUpdates(
                        chatIds,
                        linkData,
                        maxUpdate,
                        stackOverflowClient.fetchNewComments(questionId, linkData.getLastUpdate()),
                        questionTitle);

                linkData.setLastUpdate(maxUpdate);

            } catch (NumberFormatException e) {
                log.atError()
                        .addKeyValue("url", linkData.getUrl())
                        .setCause(e)
                        .log("Failed to parse StackOverflow question ID");
            }
        }
    }

    private OffsetDateTime processUpdates(
            List<Long> chatIds,
            LinkData linkData,
            OffsetDateTime currentMaxUpdate,
            Optional<StackOverflowResponse> responseOpt,
            String questionTitle) {
        OffsetDateTime newMaxUpdate = currentMaxUpdate;

        List<StackOverflowResponse.Item> items =
                responseOpt.map(StackOverflowResponse::items).orElse(null);

        if (items != null) {
            for (StackOverflowResponse.Item item : items) {
                OffsetDateTime itemDate =
                        OffsetDateTime.ofInstant(Instant.ofEpochSecond(item.creationDate()), ZoneOffset.UTC);

                String author = item.owner() != null ? item.owner().displayName() : "Unknown";
                String preview = truncateBody(item.body());

                String description = String.format(
                        scrapperMessages.getUpdates().getStackoverflowUpdate(),
                        questionTitle,
                        author,
                        itemDate,
                        preview);

                messageSender.send(new LinkUpdate()
                        .id(linkData.getId())
                        .url(linkData.getUrl())
                        .description(description)
                        .tgChatIds(chatIds));

                if (itemDate.isAfter(newMaxUpdate)) {
                    newMaxUpdate = itemDate;
                }
            }
        }
        return newMaxUpdate;
    }

    private String truncateBody(String body) {
        if (body == null || body.isBlank()) {
            return scrapperMessages.getUpdates().getStackoverflowNoDescription();
        }
        if (body.length() <= 200) {
            return body;
        }
        return body.substring(0, 197) + "...";
    }
}
