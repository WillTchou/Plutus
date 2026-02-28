package com.project.plutus.kafka.producer;

import com.project.plutus.kafka.model.KafkaTopics;
import com.project.plutus.kafka.model.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentProcessorEventProducer extends KafkaEventProducer<PaymentEvent> {
    public PaymentProcessorEventProducer(final KafkaTemplate<String, PaymentEvent> kafkaTemplates) {
        super(kafkaTemplates);
    }

    public void sendPaymentEvent(PaymentEvent event) {
        log.info("Sending payment event to topic: {} with event id: {}", KafkaTopics.PAYMENT_PROCESSOR_EVENTS, event.eventId());
        this.sendMessage(KafkaTopics.PAYMENT_PROCESSOR_EVENTS, event);
    }
}
