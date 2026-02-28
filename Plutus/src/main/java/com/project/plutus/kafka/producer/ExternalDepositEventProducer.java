package com.project.plutus.kafka.producer;

import com.project.plutus.kafka.model.KafkaTopics;
import com.project.plutus.kafka.model.AccountDepositEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExternalDepositEventProducer extends KafkaEventProducer<AccountDepositEvent> {
    public ExternalDepositEventProducer(final KafkaTemplate<String, AccountDepositEvent> kafkaTemplates) {
        super(kafkaTemplates);
    }

    public void sendExternalDepositEvent(AccountDepositEvent event) {
        log.info("Sending external deposit event to topic: {} with event id: {}", KafkaTopics.EXTERNAL_DEPOSIT_EVENTS, event.eventId());
        this.sendMessage(KafkaTopics.EXTERNAL_DEPOSIT_EVENTS, event);
    }
}
