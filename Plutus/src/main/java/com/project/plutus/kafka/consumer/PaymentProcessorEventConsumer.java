package com.project.plutus.kafka.consumer;

import com.project.plutus.account.model.Account;
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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessorEventConsumer extends KafkaEventProducer<LedgerEntryEvent> implements KafkaEventConsumer<PaymentEvent>{
    private final TransactionRepository transactionRepository;

    public PaymentProcessorEventConsumer(KafkaTemplate<String, LedgerEntryEvent> kafkaTemplate, TransactionRepository transactionRepository) {
        super(kafkaTemplate);
        this.transactionRepository = transactionRepository;
    }

    @Override
    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSOR_EVENTS, groupId = "plutus-group")
    public void consumeMessage(PaymentEvent message) {
        log.info("Received PaymentProcessorEvent with id: {}", message.eventId());
        Transaction transaction = createTransactionFromPaymentEvent(message);
        final Account sourceAccount = transaction.getSourceAccount();
        if(sourceAccount.getBalance() < transaction.getAmount()) {
            log.error("Source account with id: {} does not have enough balance for transaction id: {}",
                    sourceAccount.getId(), transaction.getId());
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new NotEnoughAmountException(String.format("Source account with id: %s does not have enough balance for transaction id: %s",
                    sourceAccount.getId(), transaction.getId()));
        }
        LedgerEntryEvent ledgerEntryEvent = new LedgerEntryEvent(UUID.randomUUID(), transaction, sourceAccount, transaction.getBeneficiary());
        log.info("Publishing ledger entry event to topic: {} for transaction id: {}", KafkaTopics.LEDGER_ENTRY_EVENTS, transaction.getId());
        sendMessage(KafkaTopics.LEDGER_ENTRY_EVENTS, ledgerEntryEvent);
    }

    private Transaction createTransactionFromPaymentEvent(PaymentEvent paymentEvent) {
        Transaction transaction = Transaction.builder()
                .amount(paymentEvent.amount())
                .beneficiary(paymentEvent.beneficiary())
                .sourceAccount(paymentEvent.account())
                .motive(paymentEvent.motive())
                .idempotencyKey(paymentEvent.idempotencyKey())
                .transactionType(TransactionType.DEBIT)
                .build();
        return transactionRepository.save(transaction);
    }
}
