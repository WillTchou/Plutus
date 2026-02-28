package com.project.plutus.kafka.consumer;

import com.project.plutus.account.model.Account;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.kafka.model.KafkaTopics;
import com.project.plutus.kafka.model.LedgerEntryEvent;
import com.project.plutus.ledger.model.LedgerEntry;
import com.project.plutus.ledger.repository.LedgerEntryRepository;
import com.project.plutus.transaction.mapper.TransactionMapper;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionStatus;
import com.project.plutus.transaction.model.TransactionType;
import com.project.plutus.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class LedgerEntryEventConsumer implements KafkaEventConsumer<LedgerEntryEvent>{
    TransactionMapper transactionMapper;
    LedgerEntryRepository ledgerEntryRepository;
    TransactionRepository transactionRepository;
    AccountRepository accountRepository;

    @Override
    @KafkaListener(topics = KafkaTopics.LEDGER_ENTRY_EVENTS, groupId = "plutus-group")
    public void consumeMessage(final LedgerEntryEvent message) {
        log.info("Received ledger entry event with id: {} for transaction id: {}", message.eventId(), message.transaction().getId());
        var transaction = message.transaction();
        addNewLedgerEntry(transaction);
        updateSucceededTransaction(transaction);
        var sourceAccount = message.sourceAccount();
        var beneficiary = message.beneficiary();
        if(!sourceAccount.equals(beneficiary.getAccount())) {
            var beneficiaryAccount = beneficiary.getAccount();
            Transaction beneficiaryTransaction = getBeneficiaryTransaction(transaction, beneficiary, sourceAccount);
            addNewLedgerEntry(beneficiaryTransaction);
            updateAccountBalance(beneficiaryAccount, beneficiaryTransaction);
        }
        updateAccountBalance(sourceAccount, transaction);
        log.info("Successfully published ledger entry event for transaction id: {}", message.transaction().getId());
    }

    private @NonNull Transaction getBeneficiaryTransaction(Transaction transaction, Beneficiary beneficiary, Account sourceAccount) {
        Transaction beneficiaryTransaction = Transaction.builder()
                .amount(transaction.getAmount())
                .beneficiary(beneficiary)
                .sourceAccount(sourceAccount)
                .motive(transaction.getMotive())
                .idempotencyKey(transaction.getIdempotencyKey() + "-beneficiary")
                .transactionType(TransactionType.CREDIT)
                .status(TransactionStatus.SUCCEEDED)
                .build();
        transactionRepository.save(beneficiaryTransaction);
        return beneficiaryTransaction;
    }

    private void updateAccountBalance(Account account, Transaction transaction) {
        account.setBalance(ledgerEntryRepository.sumAmountByAccountId(account.getId()));
        accountRepository.save(account);
        log.info("Updated account balance for ledger entry with id: {}", transaction.getId());
    }

    private void updateSucceededTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.SUCCEEDED);
        transactionRepository.save(transaction);
    }

    private void addNewLedgerEntry(Transaction transaction) {
        LedgerEntry ledgerEntry = transactionMapper.toLedgerEntry(transaction);
        ledgerEntry.setCreatedAt(LocalDateTime.now());
        ledgerEntryRepository.save(ledgerEntry);
    }
}
