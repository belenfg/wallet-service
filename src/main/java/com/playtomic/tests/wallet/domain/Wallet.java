package com.playtomic.tests.wallet.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private BigDecimal balance;

    /**
     * Constructor for creating a new Wallet with an initial balance.
     */
    public Wallet(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Adds funds to the wallet.
     *
     * @param amount the amount to add; must be positive
     * @throws IllegalArgumentException if amount is non-positive
     */
    public void topUp(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }
}
