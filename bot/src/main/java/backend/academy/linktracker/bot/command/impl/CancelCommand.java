package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.BotCommandType;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelCommand implements Command {
    private final TelegramSender sender;
    private final BotMessages messages;
    private final UserStateService userStateService;

    @Override
    public void execute(Message message) {
        long chatId = message.chat().id();

        userStateService.setState(chatId, UserState.IDLE);
        userStateService.clearTempData(chatId);

        sender.sendMessage(message.chat().id(), messages.getCancel());
    }

    @Override
    public String getName() {
        return BotCommandType.CANCEL.getName();
    }

    @Override
    public String getDescription() {
        return BotCommandType.CANCEL.getDescription();
    }
}
