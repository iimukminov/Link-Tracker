package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.sender.TelegramSender;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WrongCommand implements Command {
    private final TelegramSender sender;

    @Override
    public void execute(Update update) {
        long chatId =  update.message().chat().id();
        sender.sendMessage(chatId, "Неизвестная команда. Воспользуйтесь /help");
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
