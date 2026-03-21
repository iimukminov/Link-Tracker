package backend.academy.linktracker.bot.listeners;

import backend.academy.linktracker.bot.handlers.MessageHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotUpdateListener {
    private final TelegramBot bot;
    private final MessageHandler handler;

    @PostConstruct
    public void init() {
        log.atInfo().setMessage("Initializing BotUpdateListener").log();

        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    processUpdate(update);
                } catch (Exception e) {
                    log.atError()
                            .setMessage("Error processing update")
                            .addKeyValue("updateId", update.updateId())
                            .setCause(e)
                            .log();
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        log.atInfo().setMessage("BotUpdateListener ready").log();
    }

    public void processUpdate(Update update) {
        log.atDebug()
                .setMessage("Processing update")
                .addKeyValue("updateId", update.updateId())
                .log();

        if (update.message() != null) {
            handler.handle(update.message());
        }
    }
}
