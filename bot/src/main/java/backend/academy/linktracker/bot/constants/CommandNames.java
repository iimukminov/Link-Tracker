package backend.academy.linktracker.bot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommandNames {
    HELP("/help"),
    START("/start");

    private final String name;
}
