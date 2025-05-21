package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import com.playtomic.tests.wallet.service.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WalletControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @MockBean
    private PaymentGateway paymentGateway;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        wallet = walletRepository.save(new Wallet(new BigDecimal("20.00")));
        // Stub paymentGateway to do nothing on charge
        willDoNothing().given(paymentGateway).charge(eq(wallet.getId().toString()), any(BigDecimal.class));
    }

    @Test
    void getWallet_returnsBalance() throws Exception {
        mockMvc.perform(get("/" + wallet.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(20.00));
    }

    @Test
    void getWallet_notFound() throws Exception {
        mockMvc.perform(get("/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void topUp_success() throws Exception {
        String json = String.format(
                "{ \"walletId\": %d, \"amount\": 15.00, \"creditCardNumber\": \"4242\" }",
                wallet.getId());

        mockMvc.perform(post("/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(35.00));
    }

    @Test
    void topUp_invalidAmount() throws Exception {
        String json = String.format(
                "{ \"walletId\": %d, \"amount\": 0, \"creditCardNumber\": \"4242\" }",
                wallet.getId());

        mockMvc.perform(post("/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
