package com.codearena.code_arena_backend.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.Map;

/**
 * Global exception handler — catches exceptions not handled by individual
 * controller-level @ExceptionHandler methods.
 *
 * Prevents raw stack traces from leaking in HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles database unique-constraint violations.
     *
     * The most common case is two concurrent POST /api/auth/register requests
     * with the same username or email. Both pass the service-level existsBy*
     * check (TOCTOU window), then one of them hits the DB unique constraint and
     * Spring wraps it in DataIntegrityViolationException. Without this handler,
     * the response would be a 500 with a full Hibernate stack trace.
     *
     * We inspect the underlying constraint message to return a specific 409 body
     * so the client knows exactly which field caused the conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        String constraintName = findConstraintName(ex);

        if (matchesConstraint(constraintName, "login")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already taken"));
        }
        if (matchesConstraint(constraintName, "email")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }

        // Generic fallback — still 409 but without leaking constraint details.
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "A duplicate entry was detected"));
    }

    private String findConstraintName(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException hibernateEx) {
                return hibernateEx.getConstraintName();
            }
            current = current.getCause();
        }
        return null;
    }

    private boolean matchesConstraint(String constraintName, String token) {
        return constraintName != null && constraintName.toLowerCase(Locale.ROOT).contains(token);
    }
}
