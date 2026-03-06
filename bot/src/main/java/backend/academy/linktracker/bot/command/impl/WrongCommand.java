package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WrongCommand implements Command {
    private final TelegramSender sender;
    private final BotMessages messages;

    @Override
    public void execute(Message message) {
        long chatId = message.chat().id();
        sender.sendMessage(chatId, messages.getWrong());
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }
}
