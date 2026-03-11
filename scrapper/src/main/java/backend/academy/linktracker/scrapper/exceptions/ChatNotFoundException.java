package backend.academy.linktracker.scrapper.exceptions;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException() {
        super("Чат не зарегистрирован. Выполните регистрацию через /start.");
    }
}
