package com.project.plutus.config;

import com.project.plutus.kafka.model.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic externalDepositTopic() {
        return TopicBuilder
                .name(KafkaTopics.EXTERNAL_DEPOSIT_EVENTS)
                .build();
    }

    @Bean
    public NewTopic ledgerEntryTopic() {
        return TopicBuilder
                .name(KafkaTopics.LEDGER_ENTRY_EVENTS)
                .build();
    }

    @Bean
    public NewTopic paymentProcessorTopic() {
        return TopicBuilder
                .name(KafkaTopics.PAYMENT_PROCESSOR_EVENTS)
                .build();
    }
}
