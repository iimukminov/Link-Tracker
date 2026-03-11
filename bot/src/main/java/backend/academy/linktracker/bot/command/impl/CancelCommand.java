package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelCommand implements Command {
    private final TelegramSender sender;
    private final BotMessages messages;

    @Override
    public void execute(Message message) {
        sender.sendMessage(message.chat().id(), messages.getCancel());
    }

    @Override
    public String getName() {
        return "/cancel";
    }

    @Override
    public String getDescription() {
        return "Отменить текущее действие";
    }
}
