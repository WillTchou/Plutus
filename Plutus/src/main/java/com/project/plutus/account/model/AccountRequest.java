package com.project.plutus.account.model;

import com.project.plutus.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Value
@Builder
@AllArgsConstructor
public class AccountRequest {
    String holderName;
    String iban;
    Double amount;
    Currency currency;
}
