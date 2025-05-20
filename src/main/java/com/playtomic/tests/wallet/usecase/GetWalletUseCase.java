package com.playtomic.tests.wallet.usecase;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;

/**
 * Use case for retrieving a Wallet by its ID.
 */
public class GetWalletUseCase {

    private final WalletRepository walletRepository;

    public GetWalletUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Executes the retrieval.
     *
     * @param id the wallet identifier
     * @return the Wallet if found
     * @throws IllegalArgumentException if no wallet exists for the given id
     */
    public Wallet execute(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet with id " + id + " not found")
                );
    }
}
