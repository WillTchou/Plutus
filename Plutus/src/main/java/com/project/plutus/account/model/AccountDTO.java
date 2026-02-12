package com.project.plutus.account.model;

import com.project.plutus.model.Currency;

public record AccountDTO(String holderName, String iban, Double balance, Currency currency, AccountStatus status) {}
