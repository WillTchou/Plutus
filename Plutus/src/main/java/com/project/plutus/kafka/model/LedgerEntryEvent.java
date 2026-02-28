package com.project.plutus.kafka.model;

import com.project.plutus.account.model.Account;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionType;

import java.time.Instant;
import java.util.UUID;

public record LedgerEntryEvent(
        UUID eventId,
        Transaction transaction,
        Account sourceAccount,
        Beneficiary beneficiary
) {
}
