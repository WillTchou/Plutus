package com.project.plutus.transaction.service;

import com.project.plutus.exceptions.TransactionNotFoundException;
import com.project.plutus.transaction.mapper.TransactionMapper;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionDTO getTransactionById(final UUID transactionId, final String userEmail) {
        return transactionRepository.findById(transactionId)
                .map(getTransactionForAuthenticatedUser(userEmail))
                .map(transactionMapper::toTransactionDTO)
                .orElseThrow(TransactionNotFoundException::new);
    }

    @Override
    public Page<TransactionDTO> getTransactions(final UUID accountId, final String userEmail, final Pageable pageable) {
        final List<Transaction> transactions = transactionRepository.findAllBySourceAccountIdOrBeneficiaryAccountId(accountId)
                .stream()
                .map(getTransactionForAuthenticatedUser(userEmail))
                .toList();
        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), transactions.size());
        final List<TransactionDTO> transactionsPageContent = transactions.subList(start, end).stream()
                .map(transactionMapper::toTransactionDTO)
                .toList();
        return new PageImpl<>(transactionsPageContent, pageable, transactions.size());
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        return null;
    }

    private static @NonNull Function<Transaction, Transaction> getTransactionForAuthenticatedUser(final String userEmail) {
        return transaction -> {
            final String sourceAccountEmail = transaction.getSourceAccount().getUser().getUsername();
            final String beneficiaryEmail = transaction.getBeneficiary().getAccount().getUser().getUsername();
            if (sourceAccountEmail.equals(userEmail) || beneficiaryEmail.equals(userEmail)) {
                return transaction;
            } else {
                throw new TransactionNotFoundException();
            }
        };
    }
}
