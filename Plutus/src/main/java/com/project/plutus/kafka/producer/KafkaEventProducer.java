package com.project.plutus.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public class KafkaEventProducer<T> {
    protected final KafkaTemplate<String, T> kafkaTemplate;

    protected void sendMessage(String topic, T message) {
            kafkaTemplate.send(topic, message);
    }
}
