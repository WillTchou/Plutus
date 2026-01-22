package com.project.plutus.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.plutus.model.Currency;
import com.project.plutus.transaction.model.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false)
    private UUID id;
    @Column
    @Pattern(regexp = "^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$", message = "Invalid IBAN format")
    private String iban;
    @Column
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private Transaction transaction;
    @Column(nullable = false)
    private Double amount;
    @Column
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.EUR;
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private LedgerEntry ledgerEntry;
}
