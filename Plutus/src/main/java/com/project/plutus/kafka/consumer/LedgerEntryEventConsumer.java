package com.project.plutus.kafka.consumer;

import com.project.plutus.account.model.Account;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.account.service.AccountService;
import com.project.plutus.beneficiary.service.BeneficiaryService;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.exceptions.TransactionNotFoundException;
import com.project.plutus.kafka.model.KafkaTopics;
import com.project.plutus.kafka.model.LedgerEntryEvent;
import com.project.plutus.ledger.model.LedgerEntry;
import com.project.plutus.ledger.repository.LedgerEntryRepository;
import com.project.plutus.transaction.mapper.TransactionMapper;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionStatus;
import com.project.plutus.transaction.model.TransactionType;
import com.project.plutus.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LedgerEntryEventConsumer implements KafkaEventConsumer<LedgerEntryEvent>{
    private final TransactionMapper transactionMapper;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;

    @Override
    @KafkaListener(topics = KafkaTopics.LEDGER_ENTRY_EVENTS, groupId = "plutus-group")
    @Transactional
    public void consumeMessage(final LedgerEntryEvent message) {
        log.info("Received ledger entry event with id: {} for transaction id: {}", message.eventId(), message.transactionId());
        var transaction = transactionRepository.findById(message.transactionId())
                .orElseThrow(TransactionNotFoundException::new);
        addNewLedgerEntry(transaction);
        updateSucceededTransaction(transaction);
        var sourceAccount = accountService.getAccountEntityById(message.sourceAccountId());
        var beneficiary = beneficiaryService.getBeneficiaryEntityById(message.beneficiaryId());
        if(!sourceAccount.equals(beneficiary.getAccount())) {
            updateBeneficiaryAccountTransaction(beneficiary, transaction, sourceAccount);
        }
        updateAccountBalance(sourceAccount, transaction);
        log.info("Successfully published ledger entry event for transaction id: {}", message.transactionId());
    }

    private void updateBeneficiaryAccountTransaction(Beneficiary beneficiary, Transaction transaction, Account sourceAccount) {
        var beneficiaryAccount = beneficiary.getAccount();
        Transaction beneficiaryTransaction = getBeneficiaryTransaction(transaction, beneficiary, sourceAccount);
        addNewLedgerEntry(beneficiaryTransaction);
        updateAccountBalance(beneficiaryAccount, beneficiaryTransaction);
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
        return transactionRepository.save(beneficiaryTransaction);
    }

    private void updateAccountBalance(Account account, Transaction transaction) {
        final double balance = ledgerEntryRepository.sumAmountByAccountId(account.getId());
        account.setBalance(balance);
        accountRepository.save(account);
        log.info("Updated account balance for ledger entry with id: {}", transaction.getId());
    }

    private void updateSucceededTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.SUCCEEDED);
        transactionRepository.save(transaction);
    }

    private void addNewLedgerEntry(Transaction transaction) {
        LedgerEntry ledgerEntry = transactionMapper.toLedgerEntry(transaction);
        ledgerEntryRepository.save(ledgerEntry);
    }
}
