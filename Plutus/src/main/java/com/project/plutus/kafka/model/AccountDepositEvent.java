package com.project.plutus.kafka.model;

import com.project.plutus.account.model.Account;
import com.project.plutus.beneficiary.model.Beneficiary;

import java.time.Instant;
import java.util.UUID;

public record AccountDepositEvent(UUID eventId, Instant occurredAt, Account account, Beneficiary beneficiary,
                                  double amount, String idempotencyKey) {
}
