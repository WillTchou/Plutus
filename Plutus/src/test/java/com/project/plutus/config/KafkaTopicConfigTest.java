package com.project.plutus.config;

import com.project.plutus.kafka.model.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaTopicConfigTest {

    @Test
    void topics_areCreatedWithExpectedNames() {
        KafkaTopicConfig config = new KafkaTopicConfig();

        NewTopic external = config.externalDepositTopic();
        NewTopic ledger = config.ledgerEntryTopic();
        NewTopic payment = config.paymentProcessorTopic();

        assertEquals(KafkaTopics.EXTERNAL_DEPOSIT_EVENTS, external.name());
        assertEquals(KafkaTopics.LEDGER_ENTRY_EVENTS, ledger.name());
        assertEquals(KafkaTopics.PAYMENT_PROCESSOR_EVENTS, payment.name());
    }
}
