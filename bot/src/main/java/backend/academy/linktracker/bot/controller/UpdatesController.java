package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.sender.TelegramSender;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UpdatesController {

    private final TelegramSender telegramSender;

    @PostMapping("/updates")
    public void postUpdate(@RequestBody @Valid LinkUpdate linkUpdate) {
        log.atInfo()
                .setMessage("Received update from Scrapper")
                .addKeyValue("link_id", linkUpdate.id())
                .addKeyValue("url", linkUpdate.url())
                .addKeyValue("chats_count", linkUpdate.tgChatIds().size())
                .log();

        for (Long chatId : linkUpdate.tgChatIds()) {
            try {

                String messageText = String.format(
                        "<b>Новое обновление!</b>%n%n%s%nСсылка: %s", linkUpdate.description(), linkUpdate.url());

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
