package com.project.plutus.kafka.consumer;

public interface KafkaEventConsumer<T> {
    void consumeMessage(T message);
}
