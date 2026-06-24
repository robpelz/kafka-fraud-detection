package com.robtech.fraud_detection.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Zentrale Exception-Handling-Komponente für die gesamte Anwendung.
 * Fängt alle unerwarteten Fehler ab und gibt standardisierte Antworten zurück.
 *
 * @author Robert P.
 * @since 2026-06-24
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Behandelt Validierungsfehler bei @Valid Annotationen.
     * Wird ausgelöst, wenn ein Request-Body gegen die Validierungsregeln verstößt.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validierungsfehler: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapToValidationError)
                .toList();

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validierungsfehler: Bitte korrigieren Sie die markierten Felder",
                ex.getBindingResult().getObjectName(),
                validationErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Behandelt Constraint-Verletzungen (z.B. bei @Positive, @NotBlank).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint-Verletzung: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> ErrorResponse.ValidationError.of(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()
                ))
                .toList();

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validierungsfehler",
                "request",
                validationErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Behandelt Business-Exceptions (z.B. FraudDetectedException).
     */
    @ExceptionHandler(FraudDetectedException.class)
    public ResponseEntity<ErrorResponse> handleFraudDetectedException(FraudDetectedException ex) {
        log.warn("Betrug erkannt: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                ex.getTransactionId()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Behandelt DuplicateTransactionException (doppelte Transaktions-ID).
     */
    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTransactionException(DuplicateTransactionException ex) {
        log.warn("Doppelte Transaktion: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Transaktion existiert bereits: " + ex.getTransactionId(),
                "/api/transaction"
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Behandelt fehlerhafte JSON-Struktur (z.B. falsches Datumsformat).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Ungültiges JSON-Format: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Ungültiges JSON-Format: " + ex.getMostSpecificCause().getMessage(),
                "request"
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Behandelt Typ-Fehler bei Methodenparametern (z.B. /api/transactions/{id} mit Text).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Typ-Fehler: Parameter '{}' hat falschen Typ", ex.getName());

        String message = String.format("Parameter '%s' muss vom Typ '%s' sein",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unbekannt");

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                ex.getParameter().getDeclaringClass().getSimpleName()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Fängt alle anderen unerwarteten Exceptions ab.
     * Fallback für nicht behandelte Fehler.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unerwarteter Fehler: ", ex);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ein interner Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.",
                "unknown"
        );

        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * Hilfsmethode: Mappt Spring FieldError zu unserem ValidationError.
     */
    private ErrorResponse.ValidationError mapToValidationError(FieldError fieldError) {
        return ErrorResponse.ValidationError.of(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue()
        );
    }
}