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
    }
}
