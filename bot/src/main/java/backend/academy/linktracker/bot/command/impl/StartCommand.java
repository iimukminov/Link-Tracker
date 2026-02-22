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
public class StartCommand implements Command {

    private final TelegramSender sender;

    @Override
    public void execute(Update update) {
        long chatId = update.message().chat().id();
        String text = "Добро пожаловать! Используйте /help для списка команд";

        sender.sendMessage(chatId, text);
    }

    @Override
    public String getName() {
        return CommandNames.START.getName();
    }

    @Override
    public String getDescription() {
        return CommandDescriptions.START.getDescription();
    }
}
