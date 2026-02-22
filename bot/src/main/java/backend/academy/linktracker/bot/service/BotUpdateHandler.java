package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.CommandRegistry;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotUpdateHandler {
    private final TelegramBot bot;
    private final CommandRegistry commandRegistry;

    @PostConstruct
    public void init() {
        log.atInfo()
                .setMessage("Initializing BotUpdateHandler")
                .addKeyValue("commands_count", commandRegistry.getBotCommands().length)
                .log();

        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                processUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        log.atInfo().setMessage("BotUpdateHandler ready").log();
    }

    public void processUpdate(Update update) {
        log.atDebug()
                .setMessage("Processing update")
                .addKeyValue("update_id", update.updateId())
                .log();

        Message message = update.message();
        if (message == null || message.text() == null) {
            return;
        }
        String commandName = message.text().split("\\s+")[0];

        log.atInfo()
                .setMessage("Dispatching command")
                .addKeyValue("chatId", message.chat().id())
                .addKeyValue("command", commandName)
                .log();

        commandRegistry.getCommand(commandName).execute(update);
    }
}
