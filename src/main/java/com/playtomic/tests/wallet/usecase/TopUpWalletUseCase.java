package com.playtomic.tests.wallet.usecase;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TopUpWalletUseCase {

    private final WalletRepository walletRepository;
    private final StripeService stripeService;

    public TopUpWalletUseCase(WalletRepository walletRepository,
                              StripeService stripeService) {
        this.walletRepository = walletRepository;
        this.stripeService = stripeService;
    }

    /**
     * Charges the given credit card and tops up the wallet.
     *
     * @param id                the wallet identifier
     * @param amount            the amount to add; must be positive
     * @param creditCardNumber  the credit card number to charge
     * @return the updated Wallet
     * @throws IllegalArgumentException     if wallet not found or amount non-positive
     * @throws StripeServiceException on payment failure
     */
    public Wallet execute(Long id, BigDecimal amount, String creditCardNumber) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Wallet with id " + id + " not found"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }

        // Charge the card using StripeService
        stripeService.charge(creditCardNumber, amount);

        wallet.topUp(amount);
        return walletRepository.save(wallet);
    }
}
