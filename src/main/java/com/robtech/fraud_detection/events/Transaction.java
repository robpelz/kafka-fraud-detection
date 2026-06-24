package com.robtech.fraud_detection.events;

public record Transaction(
        String transactionId,
        String userId,
        double amount,
        String timestamp
) {
}