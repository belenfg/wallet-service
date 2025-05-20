package com.playtomic.tests.wallet.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WalletResponse {
    private BigDecimal balance;
}
