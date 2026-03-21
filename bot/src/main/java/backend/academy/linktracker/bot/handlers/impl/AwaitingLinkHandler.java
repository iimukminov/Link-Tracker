package backend.academy.linktracker.bot.handlers.impl;

import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.handlers.StateHandler;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.bot.service.validator.LinkValidator;
import com.pengrad.telegrambot.model.Message;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwaitingLinkHandler implements StateHandler {
    private final UserStateService userStateService;
    private final TelegramSender telegramSender;
    private final BotMessages messages;
    private final List<LinkValidator> validators;

    @Override
    public void handle(Message message) {
        long chatId = message.chat().id();
        String text = message.text();

        log.atInfo().addKeyValue("chatId", chatId).log("Processing link input: {}", text);

        if (!isValidLink(text, chatId)) {
            telegramSender.sendMessage(chatId, messages.getTrack().getInvalidLink());
            return;
        }

        userStateService.setTempData(chatId, "link", text);
        telegramSender.sendMessage(chatId, messages.getTrack().getTagRequest());
        userStateService.setState(chatId, UserState.AWAITING_TAGS);

        log.atInfo().addKeyValue("chatId", chatId).log("Link accepted. Transitioned to AWAITING_TAGS state");
    }

    private boolean isValidLink(String text, long chatId) {
        if (text == null || text.isBlank()) {
            log.atWarn().addKeyValue("chatId", chatId).log("Received empty link");
            return false;
        }

        try {
            URI uri = URI.create(text);
            String host = uri.getHost();

            if (host == null) {
                log.atWarn().addKeyValue("chatId", chatId).log("Could not extract host from link: {}", text);
                return false;
            }

            for (LinkValidator validator : validators) {
                if (validator.supports(host)) {
                    return true;
                }
            }

            log.atWarn().addKeyValue("chatId", chatId).addKeyValue("host", host).log("Unsupported host received");
            return false;

        } catch (IllegalArgumentException e) {
            log.atWarn().addKeyValue("chatId", chatId).log("Failed to parse URI: {}", text);
            return false;
        }
    }

    @Override
    public UserState getSupportedState() {
        return UserState.AWAITING_LINK;
    }
}
