package com.codearena.code_arena_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    /**
     * Catches validation failures triggered by @Valid annotations in @MessageMapping handlers.
     * Automatically serializes the validation details and sends them to /user/queue/errors.
     */
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("STOMP validation failed: {}", ex.getMessage());
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return Map.of(
            "error", "Validation failed",
            "details", errors
        );
    }

    /**
     * Catches all other runtime and business exceptions.
     */
    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleGenericException(Exception ex) {
        log.error("STOMP exception occurred: ", ex);
        return Map.of(
            "error", "Error processing request",
            "message", ex.getMessage() != null ? ex.getMessage() : "Unknown error"
        );
    }
}
