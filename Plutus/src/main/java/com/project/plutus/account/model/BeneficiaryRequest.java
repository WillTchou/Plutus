package com.project.plutus.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Value
@Builder
@AllArgsConstructor
public class BeneficiaryRequest {
    String holderName;
    String iban;
}
