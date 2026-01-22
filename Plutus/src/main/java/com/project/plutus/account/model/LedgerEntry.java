package com.project.plutus.account.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.plutus.transaction.model.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter
public class LedgerEntry {
    @Id
    @Column(unique = true, nullable = false)
    private Long id;
    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
