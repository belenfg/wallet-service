package com.playtomic.tests.wallet.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal balance;

    protected Wallet() {
        // JPA
    }

    public Wallet(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void topUp(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }
}
