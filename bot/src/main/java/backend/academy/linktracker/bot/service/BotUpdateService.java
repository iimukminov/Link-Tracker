package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.properties.BotMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotUpdateService {

    private final TelegramSender telegramSender;
    private final BotMessages messages;

    public void processUpdate(LinkUpdate linkUpdate) {
        if (linkUpdate.getTgChatIds() == null || linkUpdate.getTgChatIds().isEmpty()) {
            log.atWarn()
                    .setMessage("Received update with no target chat IDs")
                    .addKeyValue("link_id", linkUpdate.getId())
                    .log();
            return;
        }

        log.atInfo()
                .setMessage("Received update from Scrapper")
                .addKeyValue("link_id", linkUpdate.getId())
                .addKeyValue("url", linkUpdate.getUrl())
                .addKeyValue("chats_count", linkUpdate.getTgChatIds().size())
                .log();

        for (Long chatId : linkUpdate.getTgChatIds()) {
            try {
                String messageText =
                        String.format(messages.getUpdate(), linkUpdate.getDescription(), linkUpdate.getUrl());
                telegramSender.sendMessage(chatId, messageText);
            } catch (Exception e) {
                log.atError()
                        .setMessage("Failed to send update to chat")
                        .addKeyValue("chatId", chatId)
                        .setCause(e)
                        .log();
            }
        }
    }
}
