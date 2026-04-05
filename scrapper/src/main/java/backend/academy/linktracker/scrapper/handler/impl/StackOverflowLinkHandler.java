package backend.academy.linktracker.scrapper.handler.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import java.util.List;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowLinkHandler implements LinkHandler {

    private final StackOverflowClient stackOverflowClient;
    private final MessageSender messageSender;

    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("stackoverflow.com");
    }

    @Override
    public void handle(List<Long> chatIds, LinkData linkData) {
        String path = linkData.getUrl().getPath();
        if (path == null) return;
        String[] parts = path.split("/");

        if (parts.length >= 3 && parts[1].equals("questions")) {
            try {
                Long questionId = Long.parseLong(parts[2]);
                stackOverflowClient.fetchQuestion(questionId).ifPresent(response -> {
                    if (response.items() != null && !response.items().isEmpty()) {
                        StackOverflowResponse.Item item = response.items().get(0);
                        if (item.lastActivityDate().isAfter(linkData.getLastUpdate())) {
                            log.atInfo()
                                    .addKeyValue("question_id", questionId)
                                    .addKeyValue("chats_count", chatIds.size())
                                    .log("Update found in StackOverflow");

                            linkData.setLastUpdate(item.lastActivityDate());

                            messageSender.send(new LinkUpdate()
                                    .id(linkData.getId())
                                    .url(linkData.getUrl())
                                    .description("Обновление в вопросе " + questionId)
                                    .tgChatIds(chatIds));
                        }
                    }
                });
            } catch (NumberFormatException e) {
                log.atError().addKeyValue("url", linkData.getUrl()).log("Failed to parse StackOverflow ID");
            }
        }
    }
}
