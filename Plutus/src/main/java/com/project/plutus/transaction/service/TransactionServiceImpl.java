package com.project.plutus.transaction.service;

import com.project.plutus.account.model.Account;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.beneficiary.service.BeneficiaryService;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.exceptions.AccountNotFoundException;
import com.project.plutus.exceptions.TransactionNotFoundException;
import com.project.plutus.kafka.model.PaymentEvent;
import com.project.plutus.kafka.producer.PaymentProcessorEventProducer;
import com.project.plutus.transaction.mapper.TransactionMapper;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.model.TransactionRequest;
import com.project.plutus.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final BeneficiaryService beneficiaryService;
    private final PaymentProcessorEventProducer paymentProcessorEventProducer;

    @Override
    public TransactionDTO getTransactionById(final String transactionId, final String userEmail) {
        return transactionRepository.findById(transactionId)
                .map(getTransactionForAuthenticatedUser(userEmail))
                .map(transactionMapper::toTransactionDTO)
                .orElseThrow(TransactionNotFoundException::new);
    }

    @Override
    public Page<TransactionDTO> getTransactions(final UUID accountId, final String userEmail, final Pageable pageable) {
        if(pageable.getOffset() < 0 || pageable.getPageSize() <= 0 || pageable.getOffset() > pageable.getPageSize()) {
            throw new IndexOutOfBoundsException("Invalid pagination parameters: offset must be non-negative, page size must be greater than zero, and offset must not exceed page size.");
        }
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
    @Transactional
    public void createTransaction(final UUID accountId, final String idempotencyKey, final String userEmail,
                                  final TransactionRequest transactionRequest) {
        final Account sourceAccount = accountRepository.findByIdAndUserEmail(accountId, userEmail)
                .orElseThrow(AccountNotFoundException::new);
        final Beneficiary beneficiary = beneficiaryService.getBeneficiaryEntityById(transactionRequest.getBeneficiaryId());
        final PaymentEvent paymentEvent = new PaymentEvent(UUID.randomUUID(), Instant.now(), sourceAccount.getId(), beneficiary.getId(),
                transactionRequest.getMotive(), transactionRequest.getAmount(), idempotencyKey);
        paymentProcessorEventProducer.sendPaymentEvent(paymentEvent);
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
