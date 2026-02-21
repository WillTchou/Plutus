package com.project.plutus.kafka.consumer;

import com.project.plutus.account.model.Account;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.exceptions.NotEnoughAmountException;
import com.project.plutus.kafka.model.AccountDepositEvent;
import com.project.plutus.kafka.model.LedgerEntryEvent;
import com.project.plutus.kafka.producer.KafkaEventProducer;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionStatus;
import com.project.plutus.transaction.model.TransactionType;
import com.project.plutus.transaction.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class ExternalDepositEventConsumer extends KafkaEventProducer<LedgerEntryEvent> implements KafkaEventConsumer<AccountDepositEvent> {
    private static final String LEDGER_ENTRY_EVENTS = "ledger-entry-events";
    private static final String EXTERNAL_DEPOSIT_EVENTS = "external-deposit-events";

    private final TransactionRepository transactionRepository;

    public ExternalDepositEventConsumer(KafkaTemplate<String, LedgerEntryEvent> kafkaTemplate,
                                        TransactionRepository transactionRepository) {
        super(kafkaTemplate);
        this.transactionRepository = transactionRepository;
    }

    @Override
    @KafkaListener(topics = EXTERNAL_DEPOSIT_EVENTS, groupId = "plutus-group")
    public void consumeMessage(AccountDepositEvent message) {
        log.info("Received external deposit event with id: {}", message.eventId());
        transactionRepository.findByIdempotencyKey(message.idempotencyKey())
                .ifPresentOrElse(transaction -> log.warn("Transaction with idempotency key: {} already exists, skipping processing for event id: {}",
                                transaction.getIdempotencyKey(), message.eventId()),
                        () -> processExternalDepositEvent(message));
    }

    private void processExternalDepositEvent(AccountDepositEvent message) {
            final var transaction = createExternalDepositTransaction(message, message.beneficiary(), message.account());
            if (message.amount() >= 50) {
                transaction.setAmount(message.amount());
                final LedgerEntryEvent ledgerEntryEvent = new LedgerEntryEvent(UUID.randomUUID(), transaction);
                log.info("Publishing ledger entry event to topic: {} for transaction id: {}", LEDGER_ENTRY_EVENTS, transaction.getId());
                this.sendMessage(LEDGER_ENTRY_EVENTS, ledgerEntryEvent);
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                log.error("External deposit amount is less than 50, skipping transaction creation for event id: {}", message.eventId());
                throw new NotEnoughAmountException();
            }
    }

    private Transaction createExternalDepositTransaction(final AccountDepositEvent message,
                                                         final Beneficiary beneficiary,
                                                         final Account sourceAccount) {
        Transaction transaction = new Transaction();
        transaction.setBeneficiary(beneficiary);
        transaction.setSourceAccount(sourceAccount);
        transaction.setMotive("External deposit");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setIdempotencyKey(message.idempotencyKey());
        return transactionRepository.save(transaction);
    }
}
