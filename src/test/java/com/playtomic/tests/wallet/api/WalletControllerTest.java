package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.usecase.GetWalletUseCase;
import com.playtomic.tests.wallet.usecase.TopUpWalletUseCase;
import com.playtomic.tests.wallet.domain.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetWalletUseCase getWalletUseCase;

    @MockBean
    private TopUpWalletUseCase topUpWalletUseCase;

    @Test
    void getWallet_success() throws Exception {
        Wallet wallet = new Wallet(new BigDecimal("50.00"));
        given(getWalletUseCase.execute(1L)).willReturn(wallet);

        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    void getWallet_notFound() throws Exception {
        given(getWalletUseCase.execute(1L))
                .willThrow(new IllegalArgumentException("Wallet not found"));

        mockMvc.perform(get("/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void topUp_success() throws Exception {
        Wallet wallet = new Wallet(new BigDecimal("100.00"));
        given(topUpWalletUseCase.execute(eq(1L), eq(new BigDecimal("50.00")), eq("4242 4242 4242 4242")))
                .willReturn(wallet);

        String json = "{"
                + "\"walletId\":1,"
                + "\"amount\":50.00,"
                + "\"creditCardNumber\":\"4242 4242 4242 4242\""
                + "}";

        mockMvc.perform(post("/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void topUp_invalidAmount() throws Exception {
        given(topUpWalletUseCase.execute(eq(1L), eq(BigDecimal.ZERO), anyString()))
                .willThrow(new IllegalArgumentException("Top-up amount must be positive"));

        String json = "{"
                + "\"walletId\":1,"
                + "\"amount\":0,"
                + "\"creditCardNumber\":\"4242 4242 4242 4242\""
                + "}";

        mockMvc.perform(post("/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Top-up amount must be positive"));
    }

    @Test
    void topUp_paymentFails() throws Exception {
        given(topUpWalletUseCase.execute(eq(1L), eq(new BigDecimal("5.00")), eq("4242 4242 4242 4242")))
                .willThrow(new StripeAmountTooSmallException());

        String json = "{"
                + "\"walletId\":1,"
                + "\"amount\":5.00,"
                + "\"creditCardNumber\":\"4242 4242 4242 4242\""
                + "}";

        mockMvc.perform(post("/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
