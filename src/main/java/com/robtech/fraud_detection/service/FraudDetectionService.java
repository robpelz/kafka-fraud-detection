package com.robtech.fraud_detection.service;

import com.robtech.fraud_detection.dto.TransactionRequest;
import com.robtech.fraud_detection.events.Transaction;
import com.robtech.fraud_detection.exception.DuplicateTransactionException;
import com.robtech.fraud_detection.exception.FraudDetectedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service für die Betrugserkennung.
 * Enthält alle Regeln und die Business-Logik.
 *
 * @author Robert P.
 * @since 2026-06-24
 */
@Service
@Slf4j
public class FraudDetectionService {

    // In-Memory Speicher (für Demo-Zwecke)
    private final List<Transaction> transactions = new ArrayList<>();
    private final Set<String> transactionIds = ConcurrentHashMap.newKeySet();

    @Value("${fraud.detection.max-amount:10000}")
    private double maxAmount;

    @Value("${fraud.detection.suspicious-countries:RU,NG,UA,PK}")
    private Set<String> suspiciousCountries;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Verarbeitet eine Transaktion und prüft sie auf Betrug.
     *
     * @param request Die Transaktionsanfrage
     * @return Die verarbeitete Transaktion
     * @throws FraudDetectedException wenn Betrug erkannt wird
     * @throws DuplicateTransactionException wenn Transaktion bereits existiert
     */
    public Transaction processTransaction(TransactionRequest request) {
        // 1. Duplicate Check
        if (transactionIds.contains(request.transactionId())) {
            throw new DuplicateTransactionException(
                    "Transaktion existiert bereits: " + request.transactionId(),
                    request.transactionId()
            );
        }

        // 2. Transaction erstellen
        Transaction transaction = new Transaction(
                request.transactionId(),
                request.userId(),
                request.amount(),
                request.timestamp()
        );

        // 3. Regeln anwenden
        validateRules(transaction);

        // 4. Speichern
        transactions.add(transaction);
        transactionIds.add(transaction.transactionId());

        log.info("✅ Transaktion verarbeitet: {} für User {} mit Betrag {} €",
                transaction.transactionId(),
                transaction.userId(),
                transaction.amount());

        return transaction;
    }

    /**
     * Wendet alle Betrugsregeln auf die Transaktion an.
     */
    private void validateRules(Transaction transaction) {
        // Regel 1: Betrag prüfen
        if (transaction.amount() > maxAmount) {
            log.warn("🚨 Regel 1 verletzt: Betrag {} € über Limit {} €",
                    transaction.amount(), maxAmount);
            throw new FraudDetectedException(
                    "Betrag überschreitet Limit: " + transaction.amount() + " €",
                    transaction.transactionId(),
                    "Amount-Limit",
                    transaction.amount()
            );
        }

        // Regel 2: Nacht-Transaktionen (23:00 - 05:00)
        if (isNightTime(transaction.timestamp())) {
            log.warn("🚨 Regel 2 verletzt: Transaktion zur Nachtzeit");
            throw new FraudDetectedException(
                    "Transaktion zur Nachtzeit (23:00 - 05:00) ist verdächtig",
                    transaction.transactionId(),
                    "Night-Time",
                    transaction.amount()
            );
        }

        // Regel 3: Velocity Check (mehr als 5 Transaktionen in 5 Minuten)
        if (isVelocityExceeded(transaction)) {
            log.warn("🚨 Regel 3 verletzt: Zu viele Transaktionen in kurzer Zeit");
            throw new FraudDetectedException(
                    "Zu viele Transaktionen in kurzer Zeit (> 5 in 5 Minuten)",
                    transaction.transactionId(),
                    "Velocity-Check",
                    transaction.amount()
            );
        }

        // Regel 4: Risiko-Länder (Platzhalter für echte Geo-IP)
        // if (isSuspiciousCountry(transaction)) {
        //     throw new FraudDetectedException(...);
        // }

        log.debug("✅ Alle Regeln bestanden für Transaktion: {}", transaction.transactionId());
    }

    /**
     * Prüft ob die Uhrzeit zwischen 23:00 und 05:00 liegt.
     */
    private boolean isNightTime(String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, FORMATTER);
            int hour = dateTime.getHour();
            return hour >= 23 || hour < 5;
        } catch (Exception e) {
            log.warn("Fehler beim Parsen des Timestamps: {}", timestamp);
            return false;
        }
    }

    /**
     * Prüft ob zu viele Transaktionen in kurzer Zeit.
     */
    private boolean isVelocityExceeded(Transaction transaction) {
        try {
            LocalDateTime currentTime = LocalDateTime.parse(transaction.timestamp(), FORMATTER);
            LocalDateTime fiveMinutesAgo = currentTime.minusMinutes(5);

            long recentTransactions = transactions.stream()
                    .filter(t -> t.userId().equals(transaction.userId()))
                    .filter(t -> {
                        try {
                            LocalDateTime tTime = LocalDateTime.parse(t.timestamp(), FORMATTER);
                            return tTime.isAfter(fiveMinutesAgo) && tTime.isBefore(currentTime);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();

            return recentTransactions >= 5;
        } catch (Exception e) {
            log.warn("Fehler beim Velocity-Check: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gibt alle Transaktionen zurück.
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Gibt alle Betrugswarnungen zurück (hier: alle gespeicherten Transaktionen).
     * In einer echten Implementierung würden hier nur die Betrugsfälle zurückgegeben.
     */
    public List<Transaction> getFraudAlerts() {
        // Für Demo: Alle Transaktionen (da alle gespeicherten Transaktionen geprüft wurden)
        // In einer echten Implementierung würdest du Betrugsfälle in einer separaten Liste speichern
        return new ArrayList<>(transactions);
    }
}