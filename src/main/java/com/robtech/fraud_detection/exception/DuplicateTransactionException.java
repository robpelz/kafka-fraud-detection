package com.robtech.fraud_detection.exception;

/**
 * Wird ausgelöst, wenn eine Transaktion mit bereits existierender ID gesendet wird.
 *
 * @author Robert P.
 * @since 2026-06-24
 */
public class DuplicateTransactionException extends RuntimeException {

    private final String transactionId;

    public DuplicateTransactionException(String message, String transactionId) {
        super(message);
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}