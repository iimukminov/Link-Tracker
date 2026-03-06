package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Message;

public interface Command {
    void execute(Message message);

    String getName();

    String getDescription();
}
