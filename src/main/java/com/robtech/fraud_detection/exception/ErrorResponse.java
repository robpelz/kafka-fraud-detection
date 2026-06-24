package com.robtech.fraud_detection.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardisierte Fehlerantwort für API-Aufrufe.
 *
 * @author Robert P.
 * @since 2026-06-24
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationError> validationErrors
) {

    /**
     * Erstellt eine einfache Fehlerantwort ohne Validierungsfehler.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null
        );
    }

    /**
     * Erstellt eine Fehlerantwort mit Validierungsfehlern.
     */
    public static ErrorResponse of(int status, String error, String message, String path,
                                   List<ValidationError> validationErrors) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                validationErrors
        );
    }

    /**
     * Repräsentiert einen einzelnen Validierungsfehler.
     */
    public record ValidationError(
            String field,
            String message,
            Object rejectedValue
    ) {

        public static ValidationError of(String field, String message, Object rejectedValue) {
            return new ValidationError(field, message, rejectedValue);
        }
    }
}