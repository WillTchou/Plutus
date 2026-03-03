package com.project.plutus.kafka.model;

import java.util.UUID;

public record LedgerEntryEvent(
        UUID eventId,
        String transactionId,
        UUID sourceAccountId,
        UUID beneficiaryId
) {
}
