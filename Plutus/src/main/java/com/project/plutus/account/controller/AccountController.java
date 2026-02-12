package com.project.plutus.account.controller;

import com.project.plutus.account.model.AccountDTO;
import com.project.plutus.account.model.AccountRequest;
import com.project.plutus.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> createAccount(@RequestBody final AccountRequest accountRequest,
                                              final Authentication authentication) {
        final String userEmail = authentication.getName();
        accountService.createAccountForUser(userEmail, accountRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable("id") final UUID accountId,
                                                     final Authentication authentication) {
        final String userEmail = authentication.getName();
        return ResponseEntity.ok(accountService.getAccountById(accountId, userEmail));
    }
}
