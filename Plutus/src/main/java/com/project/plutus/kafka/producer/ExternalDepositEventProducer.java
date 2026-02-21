package com.project.plutus.kafka.producer;

import com.project.plutus.kafka.model.AccountDepositEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExternalDepositEventProducer extends KafkaEventProducer<AccountDepositEvent> {
    private static final String EXTERNAL_DEPOSIT_TOPIC = "external-deposit-events";

    public ExternalDepositEventProducer(final KafkaTemplate<String, AccountDepositEvent> kafkaTemplates) {
        super(kafkaTemplates);
    }

    public void sendExternalDepositEvent(AccountDepositEvent event) {
        log.info("Sending external deposit event to topic: {} with event id: {}", EXTERNAL_DEPOSIT_TOPIC, event.eventId());
        this.sendMessage(EXTERNAL_DEPOSIT_TOPIC, event);
    }
}
