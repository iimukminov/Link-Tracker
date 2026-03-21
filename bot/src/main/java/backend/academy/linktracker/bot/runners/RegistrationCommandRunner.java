package backend.academy.linktracker.bot.runners;

import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.command.CommandRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationCommandRunner implements CommandLineRunner {
    private final TelegramSender sender;
    private final CommandRegistry commandRegistry;

    @Override
    public void run(String... args) throws Exception {
        log.atInfo()
                .setMessage("Starting registration command")
                .addKeyValue("count", commandRegistry.getBotCommands().length)
                .log();
        sender.setMyCommands(commandRegistry.getBotCommands());
    }
}
