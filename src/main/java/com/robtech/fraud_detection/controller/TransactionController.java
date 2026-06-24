package com.robtech.fraud_detection.controller;

import com.robtech.fraud_detection.dto.TransactionRequest;
import com.robtech.fraud_detection.events.Transaction;
import com.robtech.fraud_detection.service.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller für Transaktionen und Betrugserkennung.
 *
 * @author Robert P.
 * @since 2026-06-24
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Fraud Detection", description = "API für Betrugserkennung")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final FraudDetectionService fraudDetectionService;

    @PostMapping("/transaction")
    @Operation(summary = "Neue Transaktion senden")
    public ResponseEntity<Map<String, Object>> sendTransaction(
            @Valid @RequestBody TransactionRequest request) {

        log.info("📥 Transaktion empfangen: {} von User {}",
                request.transactionId(), request.userId());

        Transaction transaction = fraudDetectionService.processTransaction(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "PROCESSED");
        response.put("transactionId", transaction.transactionId());
        response.put("amount", transaction.amount());
        response.put("message", "✅ Transaktion erfolgreich verarbeitet");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    @Operation(summary = "Alle Transaktionen anzeigen")
    public ResponseEntity<List<Transaction>> getTransactions() {
        return ResponseEntity.ok(fraudDetectionService.getAllTransactions());
    }

    @GetMapping("/fraud-alerts")
    @Operation(summary = "Alle Betrugswarnungen anzeigen")
    public ResponseEntity<List<Transaction>> getFraudAlerts() {
        return ResponseEntity.ok(fraudDetectionService.getFraudAlerts());
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiken anzeigen")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Transaction> all = fraudDetectionService.getAllTransactions();
        List<Transaction> alerts = fraudDetectionService.getFraudAlerts();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", all.size());
        stats.put("fraudAlerts", alerts.size());
        stats.put("safeTransactions", all.size() - alerts.size());
        stats.put("riskRate", all.size() > 0 ?
                (alerts.size() * 100.0 / all.size()) : 0);

        return ResponseEntity.ok(stats);
    }
}