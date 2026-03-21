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
    private String start;

    @NotNull
    @NotEmpty
    private String help;

    @NotNull
    @NotEmpty
    private String wrong;

    @NotNull
    @NotEmpty
    private String cancel;

    private Track track = new Track();
    private Untrack untrack = new Untrack();
    private List listMsg = new List();

    @Getter
    @Setter
    @Validated
    public static class Track {
        @NotNull
        @NotEmpty
        private String linkRequest;

        @NotNull
        @NotEmpty
        private String invalidLink;

        @NotNull
        @NotEmpty
        private String alreadyTracked;

        @NotNull
        @NotEmpty
        private String tagRequest;

        @NotNull
        @NotEmpty
        private String success;

        @NotNull
        @NotEmpty
        private String alreadyInProcess;

        @NotNull
        @NotEmpty
        private String lostLink;

        @NotNull
        @NotEmpty
        private String error;
    }

    @Getter
    @Setter
    @Validated
    public static class Untrack {
        @NotNull
        @NotEmpty
        private String success;

        @NotNull
        @NotEmpty
        private String notFound;

        @NotNull
        @NotEmpty
        private String usage;

        @NotNull
        @NotEmpty
        private String invalidFormat;

        @NotNull
        @NotEmpty
        private String error;
    }

    @Getter
    @Setter
    @Validated
    public static class List {
        @NotNull
        @NotEmpty
        private String empty;

        @NotNull
        @NotEmpty
        private String title;

        @NotNull
        @NotEmpty
        private String error;
    }

    @NotNull
    @NotEmpty
    private String update;

    @NotNull
    @NotEmpty
    private String badRequest;
}
