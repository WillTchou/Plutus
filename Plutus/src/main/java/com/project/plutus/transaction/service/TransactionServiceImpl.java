package com.project.plutus.transaction.service;

import com.project.plutus.exceptions.TransactionNotFoundException;
import com.project.plutus.transaction.mapper.TransactionMapper;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionDTO getTransactionById(final UUID transactionId, final UUID accountId) {
        return transactionRepository.findById(transactionId)
                .map((transaction) -> {
                    final UUID sourceAccountId = transaction.getSourceAccount().getId();
                    final UUID beneficiaryId = transaction.getBeneficiary().getAccount().getId();
                    if(sourceAccountId.equals(accountId) || beneficiaryId.equals(accountId)) {
                        return transactionMapper.toTransactionDTO(transaction);
                    } else {
                        throw new TransactionNotFoundException();
                    }
                })
                .orElseThrow(TransactionNotFoundException::new);
    }

    @Override
    public Page<TransactionDTO> getTransactions(final UUID accountId, final Pageable pageable) {
        final List<Transaction> transactions = transactionRepository.findAllBySourceAccountIdOrBeneficiaryAccountId(accountId);
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
}
