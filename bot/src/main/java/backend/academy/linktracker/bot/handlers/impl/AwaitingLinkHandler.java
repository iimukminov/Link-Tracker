package backend.academy.linktracker.bot.handlers.impl;

import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.handlers.StateHandler;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AwaitingLinkHandler implements StateHandler {
    private final UserStateService userStateService;
    private final TelegramSender telegramSender;
    private final BotMessages messages;

    @Override
    public void handle(Message message) {
        long chatId = message.chat().id();
        String text = message.text();

        if (text == null || !text.startsWith("http")) {
            telegramSender.sendMessage(chatId, messages.getTrack().getInvalidLink());
            return;
        }

        userStateService.setTempData(chatId, "link", text);

        telegramSender.sendMessage(chatId, messages.getTrack().getTagRequest());
        userStateService.setState(chatId, UserState.AWAITING_TAGS);
    }

    @Override
    public UserState getSupportedState() {
        return UserState.AWAITING_LINK;
    }
}
