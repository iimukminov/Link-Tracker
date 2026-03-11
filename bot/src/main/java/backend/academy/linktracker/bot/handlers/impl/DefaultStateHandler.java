package backend.academy.linktracker.bot.handlers.impl;

import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.handlers.StateHandler;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultStateHandler implements StateHandler {
    private final TelegramSender telegramSender;
    private final BotMessages messages;

    @Override
    public void handle(Message message) {
        telegramSender.sendMessage(message.chat().id(), messages.getWrong());
    }

    @Override
    public UserState getSupportedState() {
        return UserState.IDLE;
    }
}
