package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import com.playtomic.tests.wallet.usecase.TopUpWalletUseCase;
import com.playtomic.tests.wallet.service.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class WalletConcurrencyIT {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TopUpWalletUseCase topUpWalletUseCase;

    @MockBean
    private PaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        // Stub paymentGateway to do nothing
        willDoNothing().given(paymentGateway).charge(any(String.class), any(BigDecimal.class));
    }

    @Test
    void optimisticLockingShouldFailOneOfTwoConcurrentUpdates() throws InterruptedException {
        // Given
        Wallet wallet = walletRepository.save(new Wallet(new BigDecimal("100.00")));

        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<Throwable> thread1Exception = new AtomicReference<>();
        AtomicReference<Throwable> thread2Exception = new AtomicReference<>();

        // Thread 1: top-up 50
        Thread t1 = new Thread(() -> {
            try {
                startLatch.await();
                topUpWalletUseCase.execute(wallet.getId(), new BigDecimal("50.00"), "4242");
            } catch (Throwable e) {
                thread1Exception.set(e);
            }
        });

        // Thread 2: top-up 30
        Thread t2 = new Thread(() -> {
            try {
                startLatch.await();
                topUpWalletUseCase.execute(wallet.getId(), new BigDecimal("30.00"), "4242");
            } catch (Throwable e) {
                thread2Exception.set(e);
            }
        });

        t1.start();
        t2.start();

        // Start both threads
        startLatch.countDown();

        t1.join();
        t2.join();

        // Then one should succeed and one should fail with optimistic locking
        boolean t1Failed = thread1Exception.get() instanceof ObjectOptimisticLockingFailureException;
        boolean t2Failed = thread2Exception.get() instanceof ObjectOptimisticLockingFailureException;
        assertTrue(t1Failed ^ t2Failed, "Exactly one thread should fail due to optimistic locking");

        // And the final balance should reflect only one successful top-up
        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();
        BigDecimal balance = updated.getBalance();
        assertTrue(
                balance.equals(new BigDecimal("150.00")) || balance.equals(new BigDecimal("130.00")),
                "Final balance should be either 150.00 or 130.00, but was " + balance
        );
    }
}

