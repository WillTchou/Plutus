package com.project.plutus.transaction.repository;

import com.project.plutus.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.beneficiary.account.id = :accountId")
    List<Transaction> findAllBySourceAccountIdOrBeneficiaryAccountId(UUID accountId);
}
