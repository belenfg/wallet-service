package com.playtomic.tests.wallet.usecase;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TopUpWalletUseCaseTest {

    private WalletRepository walletRepository;
    private StripeService stripeService;
    private TopUpWalletUseCase topUpWalletUseCase;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        stripeService     = mock(StripeService.class);
        topUpWalletUseCase = new TopUpWalletUseCase(walletRepository, stripeService);
    }

    @Test
    void shouldTopUpWhenValid() throws StripeServiceException {
        Wallet wallet = new Wallet(new BigDecimal("20.00"));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        Wallet result = topUpWalletUseCase.execute(1L, new BigDecimal("30.00"), "4242 4242 4242 4242");

        assertEquals(new BigDecimal("50.00"), result.getBalance());
        verify(stripeService).charge("4242 4242 4242 4242", new BigDecimal("30.00"));
        verify(walletRepository).save(wallet);
    }

    @Test
    void shouldThrowWhenAmountNonPositive() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(new Wallet(new BigDecimal("20.00"))));

        assertThrows(IllegalArgumentException.class, () ->
                topUpWalletUseCase.execute(1L, BigDecimal.ZERO, "4242 4242 4242 4242")
        );
        verifyNoInteractions(stripeService);
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                topUpWalletUseCase.execute(2L, new BigDecimal("10.00"), "4242 4242 4242 4242")
        );
        verifyNoInteractions(stripeService);
    }

    @Test
    void shouldPropagateStripeException() throws StripeServiceException {
        Wallet wallet = new Wallet(new BigDecimal("20.00"));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        // Throw exception with no-arg constructor
        doThrow(new StripeAmountTooSmallException()).when(stripeService)
                .charge(anyString(), any(BigDecimal.class));

        assertThrows(StripeAmountTooSmallException.class, () ->
                topUpWalletUseCase.execute(1L, new BigDecimal("5.00"), "4242 4242 4242 4242")
        );
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
