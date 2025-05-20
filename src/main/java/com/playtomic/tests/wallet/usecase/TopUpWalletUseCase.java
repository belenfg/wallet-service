package com.playtomic.tests.wallet.usecase;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;

import java.math.BigDecimal;

/**
 * Use case for topping up a Wallet balance.
 */
public class TopUpWalletUseCase {

    private final WalletRepository walletRepository;

    public TopUpWalletUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Adds funds to the specified Wallet.
     *
     * @param id      the wallet identifier
     * @param amount  the amount to add; must be positive
     * @return the updated Wallet
     * @throws IllegalArgumentException if the wallet is not found or the amount is non-positive
     */
    public Wallet execute(Long id, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Wallet with id " + id + " not found"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }

        wallet.topUp(amount);
        return walletRepository.save(wallet);
    }
}
