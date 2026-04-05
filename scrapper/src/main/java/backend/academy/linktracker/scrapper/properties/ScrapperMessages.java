package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "scrapper.messages")
@Getter
@Setter
@Validated
public class ScrapperMessages {

    private Errors errors = new Errors();
    private Updates updates = new Updates();

    @Getter
    @Setter
    @Validated
    public static class Errors {
        @NotNull
        @NotEmpty
        private String badRequest;

        @NotNull
        @NotEmpty
        private String chatNotFound;

        @NotNull
        @NotEmpty
        private String linkNotFound;

        @NotNull
        @NotEmpty
        private String linkAlreadyTracked;

        @NotNull
        @NotEmpty
        private String chatAlreadyRegistered;

        @NotNull
        @NotEmpty
        private String processingError;
    }

    @Getter
    @Setter
    @Validated
    public static class Updates {
        @NotNull
        @NotEmpty
        private String githubUpdate;

        @NotNull
        @NotEmpty
        private String githubNoDescription;

        @NotNull
        @NotEmpty
        private String stackoverflowUpdate;

        @NotNull
        @NotEmpty
        private String stackoverflowNoDescription;
    }
}
