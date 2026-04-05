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

                stackOverflowClient.fetchNewAnswers(questionId, linkData.getLastUpdate()).ifPresent(response -> {
                    if (response.items() != null && !response.items().isEmpty()) {
                        OffsetDateTime maxUpdate = linkData.getLastUpdate();

                        for (StackOverflowResponse.Answer answer : response.items()) {
                            OffsetDateTime answerDate = OffsetDateTime.ofInstant(
                                Instant.ofEpochSecond(answer.creationDate()),
                                ZoneOffset.UTC
                            );

                            String author = answer.owner() != null ? answer.owner().displayName() : "Unknown";
                            String preview = truncateBody(answer.body());

                            String description = String.format(
                                scrapperMessages.getUpdates().getStackoverflowUpdate(),
                                author,
                                answerDate,
                                preview
                            );

                            messageSender.send(new LinkUpdate()
                                .id(linkData.getId())
                                .url(linkData.getUrl())
                                .description(description)
                                .tgChatIds(chatIds));

                            if (answerDate.isAfter(maxUpdate)) {
                                maxUpdate = answerDate;
                            }
                        }

                        linkData.setLastUpdate(maxUpdate);
                    }
                });
            } catch (NumberFormatException e) {
                log.atError()
                    .addKeyValue("url", linkData.getUrl())
                    .setCause(e)
                    .log("Failed to parse StackOverflow question ID");
            }
        }
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
