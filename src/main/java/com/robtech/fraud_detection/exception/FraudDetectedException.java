package com.robtech.fraud_detection.exception;

/**
 * Wird ausgelöst, wenn eine betrügerische Transaktion erkannt wird.
 *
 * @author Robert P.
 * @since 2026-06-24
 */
public class FraudDetectedException extends RuntimeException {

    private final String transactionId;
    private final String rule;
    private final double amount;

    public FraudDetectedException(String message, String transactionId, String rule, double amount) {
        super(message);
        this.transactionId = transactionId;
        this.rule = rule;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getRule() {
        return rule;
    }

    public double getAmount() {
        return amount;
    }
}