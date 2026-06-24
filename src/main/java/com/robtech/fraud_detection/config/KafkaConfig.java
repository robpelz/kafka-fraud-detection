package com.robtech.fraud_detection.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kafka-Konfigurationsklasse
 *
 * Zweck: Zentrale Konfiguration aller Kafka-bezogenen Komponenten
 * für das Fraud-Detection-System
 *
 * @author Robert P.
 * @since 2026-06-23
 * @version 1.0
 */
@Configuration
public class KafkaConfig {

    /**
     * Erstellt das Topic "transactions" für die Verarbeitung von Transaktionen
     *
     * Zweck:
     * 1. Dieses Topic empfängt alle eingehenden Transaktionsdaten
     * 2. Die Daten werden von den Fraud-Detection-Services konsumiert
     * 3. Das Topic wird automatisch beim Start der Anwendung erstellt
     *
     * Konfigurationsdetails:
     * - Topic-Name: "transactions" - beschreibt den Zweck eindeutig
     * - Partitionen: 1 - ausreichend für kleine Projekte, kann später erhöht werden
     * - Replikationsfaktor: 1 - für Entwicklungsumgebung geeignet
     *
     * Hinweis zur Skalierbarkeit:
     * - In der Produktion sollte die Partitionenzahl erhöht werden (empfohlen: 3-6)
     * - Der Replikationsfaktor sollte auf 3 erhöht werden für Hochverfügbarkeit
     *
     * @return NewTopic-Objekt mit der vollständigen Topic-Konfiguration
     * @see org.apache.kafka.clients.admin.NewTopic
     */
    @Bean
    public NewTopic createTransactionTopic() {
        // Schritt 1: Definiere den Topic-Namen
        // Schritt 2: Setze die Anzahl der Partitionen
        // Schritt 3: Setze den Replikationsfaktor
        return new NewTopic("transactions", 1, (short) 1);
    }

    /**
     * Erstellt das Topic "fraud-alerts" für Betrugswarnungen
     *
     * Zweck:
     * 1. Dieses Topic speichert alle erkannten Betrugsfälle
     * 2. Andere Services können diese Warnungen konsumieren (z.B. Benachrichtigungs-Service)
     * 3. Ermöglicht die Trennung von Transaktionsverarbeitung und Warnungsausgabe
     *
     * Konfigurationsdetails:
     * - Topic-Name: "fraud-alerts" - eindeutige Bezeichnung für Warnungen
     * - Partitionen: 1 - für einfache Verarbeitung ausreichend
     * - Replikationsfaktor: 1 - für Entwicklungsumgebung
     *
     * Status: Derzeit auskommentiert, kann bei Bedarf aktiviert werden
     *
     * @return NewTopic-Objekt für Fraud-Alerts
     */
    @Bean
     public NewTopic createFraudAlertTopic() {
        return new NewTopic("fraud-alerts", 1, (short) 1);
     }

    /**
     * Erstellt ein Dead Letter Topic für fehlgeschlagene Nachrichten
     *
     * Zweck:
     * 1. Speichert Nachrichten, die nicht erfolgreich verarbeitet wurden
     * 2. Ermöglicht spätere Fehleranalyse und manuelle Korrektur
     * 3. Verhindert Datenverlust bei Verarbeitungsfehlern
     *
     * Verwendung:
     * - Wenn eine Transaktion nicht verarbeitet werden kann, wird sie hierhin verschoben
     * - Ein separates Monitoring kann dieses Topic überwachen
     * - Ermöglicht Retry-Logik ohne Datenverlust
     *
     * Konfigurationsdetails:
     * - Topic-Name: "dead-letter-transactions" - klar erkennbar als Fehler-Topic
     * - Partitionen: 1 - für Fehleranalyse ausreichend
     * - Replikationsfaktor: 1 - für Entwicklung
     *
     * @return NewTopic-Objekt für Dead Letter Queue
     */
    // @Bean
    // public NewTopic createDeadLetterTopic() {
    //     return new NewTopic("dead-letter-transactions", 1, (short) 1);
    // }

    /**
     * Zusammenfassung der Topic-Konfigurationen:
     *
     * Aktive Topics:
     * 1. "transactions" - Haupt-Topic für Transaktionsdaten
     *
     * Geplante Topics (auskommentiert):
     * 1. "fraud-alerts" - für Betrugswarnungen
     * 2. "dead-letter-transactions" - für fehlgeschlagene Nachrichten
     *
     * Nächste Schritte:
     * 1. Bei steigender Last Partitionenzahl erhöhen
     * 2. In Produktion Replikationsfaktor auf 3 setzen
     * 3. Monitoring für alle Topics einrichten
     */
}