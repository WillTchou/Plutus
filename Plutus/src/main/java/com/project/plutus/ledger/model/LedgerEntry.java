package com.project.plutus.ledger.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.plutus.account.model.Account;
import com.project.plutus.model.Currency;
import com.project.plutus.transaction.model.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter
public final class LedgerEntry {
    @Id
    @Column(unique = true, nullable = false)
    private Long id;
    @Column(nullable = false)
    private final Double amount;
    @Column(nullable = false)
    private final Currency currency;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private final TransactionType transactionType;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private final Account account;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt = LocalDateTime.now();

    public LedgerEntry(final Double amount, final Currency currency, final TransactionType transactionType, final Account account) {
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
        this.account = account;
    }
}
