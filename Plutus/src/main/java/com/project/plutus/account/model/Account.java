package com.project.plutus.account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.model.Currency;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false)
    private UUID id;
    @Column(nullable = false)
    private String holderName;
    @Column
    @Pattern(regexp = "^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$", message = "Invalid IBAN format")
    private String iban;
    @Column
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    @Column(nullable = false)
    @Builder.Default
    private Double balance = 0.00;
    @Column
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.EUR;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private LedgerEntry ledgerEntry;
    @OneToMany(mappedBy = "sourceAccount", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Transaction> transactionsSources;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Beneficiary> beneficiaries;
}
