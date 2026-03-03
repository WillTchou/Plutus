package com.project.plutus.transaction.controller;

import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.model.TransactionRequest;
import com.project.plutus.transaction.service.TransactionService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("hasRole('USER') && hasAuthority('VERIFIED')")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable("id") final String transactionId,
                                                             final Authentication authentication) {
        final String userEmail = authentication.getName();
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId, userEmail));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') && hasAuthority('VERIFIED')")
    public ResponseEntity<Page<TransactionDTO>> getTransactions(@RequestHeader(name = "accountId") final UUID accountId,
                                                                @PageableDefault final Pageable pageable,
                                                                final Authentication authentication) {
        final String userEmail = authentication.getName();
        return ResponseEntity.ok(transactionService.getTransactions(accountId, userEmail, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') && hasAuthority('VERIFIED')")
    @RateLimiter(name = "transactionRateLimiter")
    public ResponseEntity<Void> createTransaction(@RequestHeader(name = "accountId") @NonNull final UUID accountId,
                                                  @RequestHeader(name = "idempotencyKey") @NonNull final String idempotencyKey,
                                                  @RequestBody @NonNull final TransactionRequest transactionRequest,
                                                  final Authentication authentication) {
        final String userEmail = authentication.getName();
        transactionService.createTransaction(accountId, idempotencyKey, userEmail, transactionRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
