package com.project.plutus.kafka.consumer;

import com.project.plutus.account.model.Account;
import com.project.plutus.account.service.AccountService;
import com.project.plutus.beneficiary.BeneficiaryService;
import com.project.plutus.exceptions.NotEnoughAmountException;
import com.project.plutus.kafka.model.KafkaTopics;
import com.project.plutus.kafka.model.LedgerEntryEvent;
import com.project.plutus.kafka.model.PaymentEvent;
import com.project.plutus.kafka.producer.KafkaEventProducer;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionStatus;
import com.project.plutus.transaction.model.TransactionType;
import com.project.plutus.transaction.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessorEventConsumer extends KafkaEventProducer<LedgerEntryEvent> implements KafkaEventConsumer<PaymentEvent>{
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final BeneficiaryService beneficiaryService;

    public PaymentProcessorEventConsumer(KafkaTemplate<String, LedgerEntryEvent> kafkaTemplate, TransactionRepository transactionRepository,
                                         AccountService accountService, BeneficiaryService beneficiaryService) {
        super(kafkaTemplate);
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.beneficiaryService = beneficiaryService;
    }

    @Override
    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSOR_EVENTS, groupId = "plutus-group")
    public void consumeMessage(PaymentEvent message) {
        log.info("Received PaymentProcessorEvent with id: {}", message.eventId());
        final Optional<Transaction> optionalTransaction = createTransactionFromPaymentEvent(message);
        if(optionalTransaction.isEmpty()) return;
        Transaction transaction = optionalTransaction.get();
        final Account sourceAccount = transaction.getSourceAccount();
        if(sourceAccount.getBalance() < transaction.getAmount()) {
            handleNotEnoughBalanceForTransaction(sourceAccount, transaction);
        }
        publishLedgerEntryEvent(transaction, sourceAccount);
    }

    private void publishLedgerEntryEvent(Transaction transaction, Account sourceAccount) {
        LedgerEntryEvent ledgerEntryEvent = new LedgerEntryEvent(UUID.randomUUID(), transaction.getId(), sourceAccount.getId(), transaction.getBeneficiary().getId());
        log.info("Publishing ledger entry event to topic: {} for transaction id: {}", KafkaTopics.LEDGER_ENTRY_EVENTS, transaction.getId());
        sendMessage(KafkaTopics.LEDGER_ENTRY_EVENTS, ledgerEntryEvent);
    }

    private void handleNotEnoughBalanceForTransaction(Account sourceAccount, Transaction transaction) {
        log.error("Source account with id: {} does not have enough balance for transaction id: {}",
                sourceAccount.getId(), transaction.getId());
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(transaction);
        throw new NotEnoughAmountException(String.format("Source account with id: %s does not have enough balance for transaction id: %s",
                sourceAccount.getId(), transaction.getId()));
    }

    @Transactional
    private Optional<Transaction> createTransactionFromPaymentEvent(PaymentEvent paymentEvent) {
        final var beneficiary = beneficiaryService.getBeneficiaryEntityById(paymentEvent.beneficiaryId());
        final var sourceAccount = accountService.getAccountEntityById(paymentEvent.sourceAccountId());
        Transaction transaction = Transaction.builder()
                .amount(paymentEvent.amount())
                .beneficiary(beneficiary)
                .sourceAccount(sourceAccount)
                .motive(paymentEvent.motive())
                .idempotencyKey(paymentEvent.idempotencyKey())
                .transactionType(TransactionType.DEBIT)
                .build();
        try {
            transaction = transactionRepository.save(transaction);
            log.info("Created transaction with id: {} for payment event with id: {}", transaction.getId(), paymentEvent.eventId());
            return Optional.of(transaction);
        } catch (DataIntegrityViolationException exception) {
            log.error("Transaction with idempotency key: {} already exists for payment event with id: {}",
                    paymentEvent.idempotencyKey(), paymentEvent.eventId());
            return Optional.empty();
        }
    }
}
