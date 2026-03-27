package com.project.plutus.ledger.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.plutus.account.model.Account;
import com.project.plutus.model.Currency;
import com.project.plutus.transaction.model.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
public final class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;
    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    private Currency currency;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt = LocalDateTime.now();
}
