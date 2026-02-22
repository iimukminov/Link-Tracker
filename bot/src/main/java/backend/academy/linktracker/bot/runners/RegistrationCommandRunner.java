package backend.academy.linktracker.bot.runners;

import backend.academy.linktracker.bot.command.CommandRegistry;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationCommandRunner implements CommandLineRunner {
    private final TelegramBot bot;
    private final CommandRegistry commandRegistry;

    @Override
    public void run(String... args) throws Exception {
        log.atInfo()
            .setMessage("Starting registration command")
            .addKeyValue("count", commandRegistry.getBotCommands().length)
            .log();
        SetMyCommands setMyCommands = new SetMyCommands(commandRegistry.getBotCommands());
        bot.execute(setMyCommands);
    }
}
