package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.CommandDescriptions;
import backend.academy.linktracker.bot.constants.CommandNames;
import backend.academy.linktracker.bot.sender.TelegramSender;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements Command {
    private final TelegramSender sender;

    @Override
    public void execute(Update update) {
        long chatId = update.message().chat().id();
        String text = """
        Список доступных команд:
        /start
        /help
        """;

        sender.sendMessage(chatId, text);
    }

    @Override
    public String getName() {
        return CommandNames.HELP.getName();
    }

    @Override
    public String getDescription() {
        return CommandDescriptions.HELP.getDescription();
    }
}
