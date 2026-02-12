package com.project.plutus.account.service;

import com.project.plutus.account.model.AccountDTO;
import com.project.plutus.account.model.AccountRequest;

import java.util.UUID;

public interface AccountService {
    void createAccountForUser(String userEmail, AccountRequest accountRequest);
    AccountDTO getAccountById(UUID accountId, String userEmail);
}
