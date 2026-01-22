package com.project.plutus.model;

import com.project.plutus.transaction.model.Transaction;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "idempotency_keys")
@Getter
public class IdempotencyKey {
    @Id
    @IdempotencyId
    @Column(nullable = false, unique = true)
    private String id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transaction;
}
