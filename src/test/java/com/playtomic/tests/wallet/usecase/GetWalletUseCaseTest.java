package com.playtomic.tests.wallet.usecase;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetWalletUseCaseTest {

    private WalletRepository walletRepository;
    private GetWalletUseCase getWalletUseCase;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        getWalletUseCase = new GetWalletUseCase(walletRepository);
    }

    @Test
    void shouldReturnWalletWhenItExists() {
        Wallet wallet = new Wallet(new BigDecimal("50.00"));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        Wallet result = getWalletUseCase.execute(1L);

        assertNotNull(result);
        assertEquals(wallet.getBalance(), result.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            getWalletUseCase.execute(999L);
        });
    }
}

