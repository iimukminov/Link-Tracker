// FILE: bot/src/main/java/backend/academy/linktracker/bot/handlers/MessageHandler.java

package backend.academy.linktracker.bot.handlers;

import backend.academy.linktracker.bot.command.CommandRegistry;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageHandler {
    private final CommandRegistry commandRegistry;

    public void handle(Message message) {
        if (message == null || message.text() == null || !message.text().startsWith("/")) {
            return;
        }

        log.atInfo().addKeyValue("Processing message: {}", message.text()).log();

        String commandName = extractCommandName(message.text());

        log.atInfo()
                .setMessage("Dispatching command")
                .addKeyValue("chatId", message.chat().id())
                .addKeyValue("command", commandName)
                .log();

        commandRegistry.getCommand(commandName).execute(message);
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
