package com.robtech.fraud_detection.service;

import com.robtech.fraud_detection.dto.TransactionRequest;
import com.robtech.fraud_detection.events.Transaction;
import org.springframework.stereotype.Component;

/**
 * Mappt zwischen DTO und Entity/Event.
 *
 * @author Robert P.
 * @since 2026-06-24
 */
@Component
public class TransactionMapper {

    /**
     * Konvertiert TransactionRequest zu Transaction.
     */
    public Transaction toTransaction(TransactionRequest request) {
        return new Transaction(
                request.transactionId(),
                request.userId(),
                request.amount(),
                request.timestamp()
        );
    }
}