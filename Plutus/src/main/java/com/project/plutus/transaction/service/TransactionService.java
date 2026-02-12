package com.project.plutus.transaction.service;

import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {
    TransactionDTO getTransactionById(UUID transactionId, UUID accountId);

    Page<TransactionDTO> getTransactions(UUID accountId, Pageable pageable);

    Transaction createTransaction(Transaction transaction);
}
