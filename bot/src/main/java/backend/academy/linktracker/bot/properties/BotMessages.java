package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "bot.messages")
@Getter
@Setter
@Validated
public class BotMessages {
    @NotNull
    @NotEmpty
    private String start = "Добро пожаловать!";

    @NotNull
    @NotEmpty
    private String help = """
      Доступные команды:
      /start - Запуск бота
      /help - Справка
      /track - Отслеживать ссылку
      /untrack [ссылка] - Прекратить отслеживание
      /list [тег] - Список ссылок
      """;

    @NotNull
    @NotEmpty
    private String wrong = "Неизвестная команда";

    @NotNull
    @NotEmpty
    private String cancel = "Действие отменено";

    private Track track = new Track();
    private Untrack untrack = new Untrack();
    private List listMsg = new List();

    @Getter
    @Setter
    @Validated
    public static class Track {
        @NotNull
        @NotEmpty
        private String linkRequest = "Отправьте ссылку";

        @NotNull
        @NotEmpty
        private String invalidLink = "Некорректная ссылка";

        @NotNull
        @NotEmpty
        private String alreadyTracked = "Уже отслеживается";

        @NotNull
        @NotEmpty
        private String tagRequest = "Теги или /cancel";

        @NotNull
        @NotEmpty
        private String success = "Добавлено";
    }

    @Getter
    @Setter
    @Validated
    public static class Untrack {
        @NotNull
        @NotEmpty
        private String success = "Удалено";

        @NotNull
        @NotEmpty
        private String notFound = "Не найдена";
    }

    @Getter
    @Setter
    @Validated
    public static class List {
        @NotNull
        @NotEmpty
        private String empty = "Нет ссылок";

        @NotNull
        @NotEmpty
        private String title = "Ваши ссылки: ";
    }
}
