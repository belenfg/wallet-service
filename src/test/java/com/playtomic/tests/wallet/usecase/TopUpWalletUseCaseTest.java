package com.playtomic.tests.wallet.usecase;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TopUpWalletUseCaseTest {

    private WalletRepository walletRepository;
    private TopUpWalletUseCase topUpWalletUseCase;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        topUpWalletUseCase = new TopUpWalletUseCase(walletRepository);
    }

    @Test
    void shouldIncreaseBalanceWhenValidAmount() {
        // Given
        Wallet wallet = new Wallet(new BigDecimal("20.00"));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Wallet result = topUpWalletUseCase.execute(1L, new BigDecimal("30.00"));

        // Then balance is updated and saved
        assertEquals(new BigDecimal("50.00"), result.getBalance());
        verify(walletRepository).save(wallet);
    }

    @Test
    void shouldThrowExceptionForNonPositiveAmount() {
        // Given an existing wallet...
        when(walletRepository.findById(1L)).thenReturn(Optional.of(new Wallet(new BigDecimal("20.00"))));

        // When/Then invalid top-up amount
        assertThrows(IllegalArgumentException.class,
                () -> topUpWalletUseCase.execute(1L, BigDecimal.ZERO));
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        // Given no wallet found...
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> topUpWalletUseCase.execute(2L, new BigDecimal("10.00")));
    }
}

