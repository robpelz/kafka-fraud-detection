package com.robtech.fraud_detection.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object für eingehende Transaktionsanfragen.
 * Enthält Validierungsregeln für alle Felder.
 *
 * @author Robert P.
 * @since 2026-06-24
 */
public record TransactionRequest(

        @NotBlank(message = "Transaction ID ist erforderlich")
        @Pattern(regexp = "^tx-[a-zA-Z0-9]+$", message = "Transaction ID muss mit 'tx-' beginnen")
        String transactionId,

        @NotBlank(message = "User ID ist erforderlich")
        @Pattern(regexp = "^user-[a-zA-Z0-9]+$", message = "User ID muss mit 'user-' beginnen")
        String userId,

        @NotNull(message = "Betrag ist erforderlich")
        @Min(value = 1, message = "Betrag muss größer als 0 sein")
        Double amount,

        @NotBlank(message = "Zeitstempel ist erforderlich")
        @Pattern(
                regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$",
                message = "Zeitstempel muss im Format yyyy-MM-ddTHH:mm:ss sein"
        )
        String timestamp
) {
}