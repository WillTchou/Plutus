package com.project.plutus.kafka.consumer;

import com.project.plutus.kafka.model.LedgerEntryEvent;
import com.project.plutus.ledger.model.LedgerEntry;
import com.project.plutus.ledger.repository.LedgerEntryRepository;
import com.project.plutus.transaction.mapper.TransactionMapper;
import com.project.plutus.transaction.model.TransactionStatus;
import com.project.plutus.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class LedgerEntryEventConsumer implements KafkaEventConsumer<LedgerEntryEvent>{
    private static final String LEDGER_ENTRY_EVENTS = "ledger-entry-events";

    TransactionMapper transactionMapper;
    LedgerEntryRepository ledgerEntryRepository;
    TransactionRepository transactionRepository;

    @Override
    @KafkaListener(topics = LEDGER_ENTRY_EVENTS, groupId = "plutus-group")
    public void consumeMessage(LedgerEntryEvent message) {
        log.info("Received ledger entry event with id: {} for transaction id: {}", message.eventId(), message.transaction().getId());
        LedgerEntry ledgerEntry = transactionMapper.toLedgerEntry(message.transaction());
        ledgerEntry.setCreatedAt(LocalDateTime.now());
        ledgerEntryRepository.save(ledgerEntry);
        var transaction = message.transaction();
        transaction.setStatus(TransactionStatus.SUCCEEDED);
        transactionRepository.save(transaction);
        log.info("Successfully published ledger entry event for transaction id: {}", message.transaction().getId());
    }
}
