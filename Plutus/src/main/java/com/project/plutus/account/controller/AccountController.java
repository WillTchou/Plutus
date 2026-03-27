package com.project.plutus.account.controller;

import com.project.plutus.account.model.AccountDTO;
import com.project.plutus.account.model.AccountRequest;
import com.project.plutus.account.model.BeneficiaryRequest;
import com.project.plutus.account.service.AccountService;
import jakarta.annotation.Nullable;
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
    @PreAuthorize("hasRole('USER') && hasAuthority('VERIFIED')")
    public ResponseEntity<UUID> createAccount(@RequestBody final AccountRequest accountRequest,
                                              @RequestHeader @Nullable final String idempotencyKey,
                                              final Authentication authentication) {
        final String userEmail = authentication.getName();
        return new ResponseEntity<>(accountService.createAccountForUser(userEmail, accountRequest, idempotencyKey), HttpStatus.CREATED);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("hasRole('USER') && hasAuthority('VERIFIED')")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable("id") final UUID accountId,
                                                     final Authentication authentication) {
        final String userEmail = authentication.getName();
        return ResponseEntity.ok(accountService.getAccountById(accountId, userEmail));
    }

    @PostMapping(path = "/beneficiary")
    @PreAuthorize("hasRole('USER') && hasAuthority('VERIFIED')")
    public ResponseEntity<UUID> addBeneficiary(@RequestBody final BeneficiaryRequest beneficiaryRequest,
                                               @RequestHeader UUID accountId,
                                               final Authentication authentication) {
        final String userEmail = authentication.getName();
        accountService.addBeneficiary(userEmail, accountId, beneficiaryRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
