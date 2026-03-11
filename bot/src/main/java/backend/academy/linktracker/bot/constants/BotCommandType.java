package backend.academy.linktracker.bot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BotCommandType {
    HELP("/help", "Список доступных команд"),
    START("/start", "Старт бота"),
    TRACK("/track", "Отслеживать ссылку"),
    UNTRACK("/untrack", "Прекратить отслеживание"),
    LIST("/list", "Список отслеживаемых ссылок");

    private final String name;
    private final String description;
}
