package backend.academy.linktracker.bot.handlers;

import backend.academy.linktracker.bot.constants.UserState;
import com.pengrad.telegrambot.model.Message;

public interface StateHandler {
    void handle(Message message);

    UserState getSupportedState();
}
