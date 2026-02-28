package com.project.plutus.kafka.model;

public final class KafkaTopics {
    public static final String EXTERNAL_DEPOSIT_EVENTS = "external-deposit-events";
    public static final String LEDGER_ENTRY_EVENTS = "ledger-entry-events";
    public static final String PAYMENT_PROCESSOR_EVENTS = "payment-processor-events";

    private KafkaTopics() {
    }
}
