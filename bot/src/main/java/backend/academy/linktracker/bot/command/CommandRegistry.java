package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.command.impl.WrongCommand;
import com.pengrad.telegrambot.model.BotCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandRegistry {

    private final List<Command> commands;
    private final Map<String, Command> commandMap = new ConcurrentHashMap<>();
    private final WrongCommand wrongCommand;

    @PostConstruct
    public void init() {
        log.atInfo()
            .setMessage("Registering commands")
            .addKeyValue("total", commands.size())
            .log();
        for (Command command : commands) {
            if (!command.getName().isEmpty()) {
                commandMap.put(command.getName().toLowerCase(), command);
                log.atInfo()
                    .setMessage("Command registered")
                    .addKeyValue("name", command.getName())
                    .log();
            }
        }
    }

    public Command getCommand(String commandName) {
        return commandMap.getOrDefault(commandName.toLowerCase(), wrongCommand);
    }

    public BotCommand[] getBotCommands() {
        BotCommand[] botCommands = new BotCommand[commands.size() - 1];
        int i = 0;
        for (Command command : commands) {
            if (!command.getName().isEmpty()) {
                botCommands[i] = new BotCommand(command.getName(), command.getDescription());
                i++;
            }
        }
        return botCommands;
    }

}
