package backend.academy.linktracker.bot.service.validator;

public interface LinkValidator {
    boolean supports(String host);
}
