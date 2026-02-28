package com.project.plutus.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.UUID;

@Data
@Value
@Builder
@AllArgsConstructor
public class TransactionRequest {
    double amount;
    String motive;
    UUID beneficiaryId;
}
