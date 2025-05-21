package com.playtomic.tests.wallet.usecase;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import com.playtomic.tests.wallet.service.PaymentException;
import com.playtomic.tests.wallet.service.PaymentGateway;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TopUpWalletUseCase {

    private final WalletRepository walletRepository;
    private final PaymentGateway paymentGateway;

    public TopUpWalletUseCase(WalletRepository walletRepository,
                              PaymentGateway paymentGateway) {
        this.walletRepository = walletRepository;
        this.paymentGateway = paymentGateway;
    }

    /**
     * Charges the given credit card and tops up the wallet.
     *
     * @param id                the wallet identifier
     * @param amount            the amount to add; must be positive
     * @param creditCardNumber  the credit card number to charge
     * @return the updated Wallet
     * @throws IllegalArgumentException if wallet not found or amount non-positive
     * @throws PaymentException         on payment failure
     */
    public Wallet execute(Long id, BigDecimal amount, String creditCardNumber) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Wallet with id " + id + " not found"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }

        // Charge via abstract gateway
        paymentGateway.charge(creditCardNumber, amount);

        wallet.topUp(amount);
        return walletRepository.save(wallet);
    }
}
