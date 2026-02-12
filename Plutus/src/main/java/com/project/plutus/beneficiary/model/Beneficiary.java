package com.project.plutus.beneficiary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.plutus.account.model.Account;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "beneficiaries")
@Data
@NoArgsConstructor
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false)
    private UUID id;
    @Column(nullable = false)
    private String holderName;
    @Column(nullable = false)
    private String iban;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Transaction> transactions;
}
