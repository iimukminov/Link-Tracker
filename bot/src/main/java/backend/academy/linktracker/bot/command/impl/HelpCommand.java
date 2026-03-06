package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.BotCommandType;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements Command {
    private final TelegramSender sender;
    private final BotMessages messages;

    @Override
    public void execute(Message message) {
        long chatId = message.chat().id();
        sender.sendMessage(chatId, messages.getHelp());
    }

    @Override
    public String getName() {
        return BotCommandType.HELP.getName();
    }

    @Override
    public String getDescription() {
        return BotCommandType.HELP.getDescription();
    }
}
