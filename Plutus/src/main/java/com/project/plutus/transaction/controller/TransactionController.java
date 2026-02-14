package com.project.plutus.transaction.controller;

import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable("id") final UUID transactionId,
                                                             final Authentication authentication) {
        final String userEmail = authentication.getName();
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId, userEmail));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionDTO>> getTransactions(@RequestHeader(name = "accountId") final UUID accountId,
                                                                @PageableDefault final Pageable pageable,
                                                                final Authentication authentication) {
        final String userEmail = authentication.getName();
        return ResponseEntity.ok(transactionService.getTransactions(accountId, userEmail, pageable));
    }
}
