package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.command.impl.WrongCommand;
import com.pengrad.telegrambot.model.BotCommand;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandRegistry {
    private final Map<String, Command> commandMap;
    private final Command wrongCommand;

    public CommandRegistry(List<Command> commands, WrongCommand wrongCommand) {
        log.atInfo()
                .setMessage("Registering command")
                .addKeyValue("commands", commands.stream().map(Command::getName).toList())
                .log();

        this.wrongCommand = wrongCommand;
        this.commandMap = commands.stream()
                .filter(command -> !command.getName().isEmpty())
                .collect(Collectors.toConcurrentMap(
                        command -> command.getName().toLowerCase(), command -> command, (existing, replacement) -> {
                            log.atInfo()
                                    .addKeyValue("Duplicate command: {}", replacement.getName())
                                    .log();
                            return existing;
                        }));
    }

    public Command getCommand(String commandName) {
        return commandMap.getOrDefault(commandName.toLowerCase(), wrongCommand);
    }

    public BotCommand[] getBotCommands() {
        return commandMap.values().stream()
                .map(command -> new BotCommand(command.getName(), command.getDescription()))
                .toArray(BotCommand[]::new);
    }
}
