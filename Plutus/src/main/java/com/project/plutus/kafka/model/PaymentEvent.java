package com.project.plutus.kafka.model;

import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(UUID eventId, Instant occurredAt, UUID sourceAccountId, UUID beneficiaryId, String motive,
                           double amount, String idempotencyKey) {
}
