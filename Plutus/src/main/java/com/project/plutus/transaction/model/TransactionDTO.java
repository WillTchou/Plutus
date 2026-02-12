package com.project.plutus.transaction.model;

import com.project.plutus.beneficiary.model.BeneficiaryDTO;
import com.project.plutus.model.Currency;

public record TransactionDTO(Double amount, Currency currency, String motive, String createdAt, TransactionType type,
                             TransactionStatus status, BeneficiaryDTO beneficiary) {
}
