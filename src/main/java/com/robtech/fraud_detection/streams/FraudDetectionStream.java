package com.robtech.fraud_detection.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robtech.fraud_detection.events.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

/**
 * Fraud Detection Stream Processing Klasse
 *
 * Zweck: Verarbeitet eingehende Transaktionen in Echtzeit und erkennt
 * betrügerische Aktivitäten mittels Kafka Streams
 *
 * Verarbeitungsablauf:
 * 1. Liest Transaktionen vom Topic "transactions"
 * 2. Wendet Betrugserkennungsregeln an (Filter)
 * 3. Schreibt erkannte Betrugsfälle in das Topic "fraud-alerts"
 *
 * @author Robert P.
 * @since 2026-06-23
 * @version 1.0
 */
@Configuration
@EnableKafkaStreams
@Slf4j
public class FraudDetectionStream {

    /**
     * Hauptmethode zur Erstellung des Fraud Detection Streams
     *
     * Schritt-für-Schritt Verarbeitung:
     * Schritt 1: Lese Nachrichten vom Input-Topic "transactions"
     * Schritt 2: Verarbeite den Stream und filtere betrügerische Transaktionen
     * Schritt 3: Schreibe die erkannten Betrugsfälle in das Output-Topic "fraud-alerts"
     *
     * Stream-Processing-Pipeline:
     * [Input] transactions -> [Filter] isSuspicious() -> [Log] Warnung -> [Output] fraud-alerts
     *
     * @param builder StreamsBuilder für die Erstellung der Kafka-Streams-Pipeline
     * @return KStream der Transaktionen für weitere Verarbeitung (wenn nötig)
     */
    @Bean
    public KStream<String, String> fraudDetectStream(StreamsBuilder builder) {

        // Schritt 1: Lese Nachrichten vom Input-Topic
        // Der Stream wird mit den Schlüssel-Wert-Paaren aus "transactions" befüllt
        KStream<String, String> transactionsStream = builder
                .stream("transactions");

        // Schritt 2: Verarbeite den Stream zur Erkennung betrügerischer Transaktionen
        // - filter: Wendet die Betrugserkennungslogik an
        // - peek: Loggt Warnungen für erkannte Betrugsfälle
        KStream<String, String> fraudTransactionStream = transactionsStream
                .filter((key, value) -> isSuspicious(value))
                .peek((key, value) -> {
                    log.warn("⚠️ FRAUD ALERT - transactionId={} , value={}", key, value);
                });

        // Schritt 3: Schreibe erkannte betrügerische Transaktionen in das Output-Topic
        // Das Topic "fraud-alerts" wird von anderen Services konsumiert (z.B. Notification-Service)
        fraudTransactionStream.to("fraud-alerts");

        // Rückgabe des Streams für mögliche weitere Verarbeitung
        return transactionsStream;
    }

    /**
     * Alternative funktionale Schreibweise (kompakter)
     *
     * Vorteile:
     * 1. Weniger Code, gleiche Funktionalität
     * 2. Direkte Verkettung der Operationen
     * 3. Leichter lesbar für erfahrene Entwickler
     *
     * Nachteile:
     * 1. Weniger explizite Variablen für Debugging
     * 2. Schwerer zu erweitern für komplexe Logik
     *
     * Empfehlung: Für einfache Streams wie diesen ist der funktionale Stil geeignet
     * Für komplexe Verarbeitung mit mehreren Schritten den ausführlichen Stil verwenden
     */
    // public void fraudDetectStreamFunctionalStyle(StreamsBuilder builder) {
    //     builder
    //         .stream("transactions")
    //         .filter((key, value) -> isSuspicious((String) value))
    //         .peek((key, value) -> log.warn("⚠️ FRAUD ALERT - transactionId={}, value={}", key, value))
    //         .to("fraud-alerts");
    // }

    /**
     * Prüft, ob eine Transaktion verdächtig ist (Betrugserkennungslogik)
     *
     * Schritt-für-Schritt:
     * 1. Versuche die JSON-Nachricht in ein Transaction-Objekt zu parsen
     * 2. Wende die Betrugsregeln an
     * 3. Bei Fehlern: Nachricht als sicher behandeln (false zurückgeben)
     *
     * Aktuelle Betrugsregeln:
     * - Transaktionsbetrag > 10.000 € (einfache Regel)
     *
     * Erweiterungsmöglichkeiten für zukünftige Regeln:
     * - Zu viele Transaktionen in kurzer Zeit (Velocity Check)
     * - Ungewöhnliche Länder oder IP-Adressen
     * - Tageslimits überschritten
     * - ML-Modell-basierte Erkennung
     * - Risikobewertung basierend auf Benutzerhistorie
     *
     * @param value JSON-String der Transaktion
     * @return true wenn Betrug erkannt, false wenn Transaktion sicher ist
     */
    private boolean isSuspicious(String value) {
        try {
            // Schritt 1: JSON parsen
            Transaction transaction = new ObjectMapper()
                    .readValue(value, Transaction.class);

            // Schritt 2: Betrugsregel anwenden
            // ACHTUNG: 10000 ist ein Beispiel-Wert - in Produktion über application.yml konfigurierbar machen
            return transaction.amount() > 10000; // einfache Betrugsregel: Betrag über 10.000 €

        } catch (Exception e) {
            // Schritt 3: Bei Fehler keine Betrugswarnung auslösen
            // Log für Debugging, aber keine Unterbrechung der Verarbeitung
            log.debug("Fehler beim Parsen der Transaktion: {}", e.getMessage());
            return false;
        }
    }
}