package com.project.plutus.transaction.service;

import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.model.TransactionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {
    TransactionDTO getTransactionById(String transactionId, String userEmail);

    Page<TransactionDTO> getTransactions(UUID accountId, String userEmail,Pageable pageable);

    void createTransaction(UUID accountId, String idempotencyKey, String userEmail, TransactionRequest transactionRequest);
}
