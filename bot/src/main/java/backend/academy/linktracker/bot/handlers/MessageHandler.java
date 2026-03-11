package backend.academy.linktracker.bot.handlers;

import backend.academy.linktracker.bot.command.CommandRegistry;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageHandler {
    private final CommandRegistry commandRegistry;
    private final UserStateService userStateService;
    private final StateHandlerRegistry stateHandlerRegistry;

    public void handle(Message message) {
        if (message == null || message.text() == null) {
            return;
        }

        long chatId = message.chat().id();

        if (message.text().startsWith("/")) {
            userStateService.setState(chatId, UserState.IDLE);
            userStateService.clearTempData(chatId);
            handleCommand(message);
        } else {
            handleNonCommand(message);
        }
    }

    private void handleCommand(Message message) {
        String commandName = extractCommandName(message.text());

        log.atInfo()
                .setMessage("Dispatching command")
                .addKeyValue("chatId", message.chat().id())
                .addKeyValue("command", commandName)
                .addKeyValue("fullText", message.text())
                .log();

        commandRegistry.getCommand(commandName).execute(message);
    }

    private void handleNonCommand(Message message) {
        long chatId = message.chat().id();

        log.atInfo()
                .setMessage("Dispatching non command")
                .addKeyValue("chatId", chatId)
                .addKeyValue("handler", userStateService.getState(chatId))
                .addKeyValue("fullText", message.text())
                .log();

        stateHandlerRegistry.getHandler(userStateService.getState(chatId)).handle(message);
    }

    private String extractCommandName(String text) {
        String command = text.split("\\s+")[0].toLowerCase().trim();

        int atIndex = command.indexOf('@');
        if (atIndex != -1) {
            return command.substring(0, atIndex);
        }

        return command;
    }
}
