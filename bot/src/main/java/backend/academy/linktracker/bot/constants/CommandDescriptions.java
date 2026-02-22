package backend.academy.linktracker.bot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommandDescriptions {
    HELP("Список доступных команд"),
    START("Старт бота");

    private final String description;
}
