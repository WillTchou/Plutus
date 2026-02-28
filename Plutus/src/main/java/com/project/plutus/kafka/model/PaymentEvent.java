package com.project.plutus.kafka.model;

import com.project.plutus.account.model.Account;
import com.project.plutus.beneficiary.model.Beneficiary;

import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(UUID eventId, Instant occurredAt, Account account, Beneficiary beneficiary, String motive,
                           double amount, String idempotencyKey) {
}
