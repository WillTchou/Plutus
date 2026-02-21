package com.project.plutus.transaction.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.plutus.account.model.Account;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.model.Currency;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {
    @Id
    @TransactionId
    @Column(nullable = false, unique = true)
    private String id;
    @Column(nullable = false, unique = true)
    private String idempotencyKey;
    @Column(nullable = false)
    private Double amount;
    @Column(nullable = false)
    private Currency currency = Currency.EUR;
    @Column
    private String motive;
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Column
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "source_account_id", referencedColumnName = "id")
    private Account sourceAccount;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "beneficiary_id", referencedColumnName = "id")
    private Beneficiary beneficiary;
}
