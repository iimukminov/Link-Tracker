package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.exceptions.RateLimitExceededException;
import backend.academy.linktracker.bot.properties.BotMessages;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final BotMessages messages;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleOtherExceptions(Exception ex) {
        log.atError().setCause(ex).log("Unhandled exception occurred in Bot API");

        return createErrorResponse(ex, messages.getBadRequest(), String.valueOf(HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiErrorResponse handleRateLimitExceeded(RateLimitExceededException ex) {
        log.atWarn().log("Rate limit exceeded in Bot API: {}", ex.getMessage());

        return createErrorResponse(ex, messages.getRateLimitExceeded(), String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    private ApiErrorResponse createErrorResponse(Exception ex, String description, String code) {
        List<String> stacktrace = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();

        return new ApiErrorResponse()
                .description(description)
                .code(code)
                .exceptionName(ex.getClass().getSimpleName())
                .exceptionMessage(ex.getMessage())
                .stacktrace(stacktrace);
    }
}
