package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exceptions.*;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
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

    private final ScrapperMessages messages;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleOtherExceptions(Exception ex) {
        log.atError().setCause(ex).log("Unhandled exception occurred in Scrapper API");
        return createErrorResponse(ex, messages.getErrors().getBadRequest(), String.valueOf(HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ChatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleChatNotFound(ChatNotFoundException ex) {
        log.atWarn().log("Chat not found: {}", ex.getMessage());
        return createErrorResponse(ex, messages.getErrors().getChatNotFound(), String.valueOf(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleLinkNotFound(LinkNotFoundException ex) {
        log.atWarn().log("Link not found: {}", ex.getMessage());
        return createErrorResponse(ex, messages.getErrors().getLinkNotFound(), String.valueOf(HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleLinkAlreadyTracked(LinkAlreadyTrackedException ex) {
        log.atWarn().log("Link already tracked: {}", ex.getMessage());
        return createErrorResponse(ex, messages.getErrors().getLinkAlreadyTracked(), String.valueOf(HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(ChatAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleChatAlreadyRegistered(ChatAlreadyRegisteredException ex) {
        log.atWarn().log("Chat already registered: {}", ex.getMessage());
        return createErrorResponse(ex, messages.getErrors().getChatAlreadyRegistered(), String.valueOf(HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiErrorResponse handleRateLimitExceeded(RateLimitExceededException ex) {
        log.atWarn().log("Rate limit exceeded: {}", ex.getMessage());
        return createErrorResponse(ex, messages.getErrors().getRateLimitExceeded(), String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    private ApiErrorResponse createErrorResponse(Exception ex, String description, String code) {
        List<String> stackTrace = Arrays.stream(ex.getStackTrace())
            .map(StackTraceElement::toString)
            .toList();

        return new ApiErrorResponse()
            .description(description)
            .code(code)
            .exceptionName(ex.getClass().getSimpleName())
            .exceptionMessage(ex.getMessage())
            .stacktrace(stackTrace);
    }
}
