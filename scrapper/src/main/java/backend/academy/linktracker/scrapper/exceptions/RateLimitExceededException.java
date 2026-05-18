package backend.academy.linktracker.scrapper.exceptions;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
