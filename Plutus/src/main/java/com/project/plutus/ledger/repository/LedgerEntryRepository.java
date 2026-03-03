package com.project.plutus.ledger.repository;

import com.project.plutus.ledger.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    @Query("SELECT SUM(le.amount) FROM LedgerEntry le WHERE le.account.id = :accountId")
    double sumAmountByAccountId(UUID accountId);
}
