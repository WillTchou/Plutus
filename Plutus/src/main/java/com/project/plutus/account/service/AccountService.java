package com.project.plutus.account.service;

import com.project.plutus.account.model.Account;
import com.project.plutus.account.model.AccountDTO;
import com.project.plutus.account.model.AccountRequest;

import java.util.UUID;

public interface AccountService {
    void createAccountForUser(String userEmail, AccountRequest accountRequest, String idempotencyKey);
    AccountDTO getAccountById(UUID accountId, String userEmail);
    Account getAccountEntityById(UUID accountId);
}
