package com.project.plutus.kafka.model;

import java.time.Instant;
import java.util.UUID;

public record AccountDepositEvent(UUID eventId, Instant occurredAt, UUID sourceAccountId, UUID beneficiaryId,
                                  double amount, String idempotencyKey) {
}
