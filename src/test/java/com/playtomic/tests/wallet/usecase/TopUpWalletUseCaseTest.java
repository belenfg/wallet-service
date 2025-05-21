package com.playtomic.tests.wallet.usecase;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import com.playtomic.tests.wallet.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TopUpWalletUseCaseTest {

    private WalletRepository walletRepository;
    private PaymentGateway paymentGateway;
    private TopUpWalletUseCase topUpWalletUseCase;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        paymentGateway = mock(PaymentGateway.class);
        topUpWalletUseCase = new TopUpWalletUseCase(walletRepository, paymentGateway);
    }

    @Test
    void shouldTopUpWhenValid() {
        Wallet wallet = new Wallet(new BigDecimal("20.00"));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        Wallet result = topUpWalletUseCase.execute(1L, new BigDecimal("30.00"), "4242 4242 4242 4242");

        assertEquals(new BigDecimal("50.00"), result.getBalance());
        verify(paymentGateway).charge("4242 4242 4242 4242", new BigDecimal("30.00"));
        verify(walletRepository).save(wallet);
    }

    @Test
    void shouldThrowWhenAmountNonPositive() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(new Wallet(new BigDecimal("20.00"))));

        assertThrows(IllegalArgumentException.class, () ->
                topUpWalletUseCase.execute(1L, BigDecimal.ZERO, "4242 4242 4242 4242")
        );
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                topUpWalletUseCase.execute(2L, new BigDecimal("10.00"), "4242 4242 4242 4242")
        );
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void shouldPropagatePaymentException() {
        Wallet wallet = new Wallet(new BigDecimal("20.00"));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        doThrow(new PaymentException("Payment failed")).when(paymentGateway)
                .charge(anyString(), any(BigDecimal.class));

        PaymentException ex = assertThrows(PaymentException.class, () ->
                topUpWalletUseCase.execute(1L, new BigDecimal("5.00"), "4242 4242 4242 4242")
        );
        assertEquals("Payment failed", ex.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
