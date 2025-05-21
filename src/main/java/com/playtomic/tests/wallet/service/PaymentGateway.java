package com.playtomic.tests.wallet.service;

import java.math.BigDecimal;

/**
 * Abstraction over any payment provider.
 */
public interface PaymentGateway {
    /**
     * Charges the specified amount on the given credit card.
     *
     * @param creditCardNumber the credit card number
     * @param amount the amount to charge
     * @throws PaymentException on failure.
     */
    void charge(String creditCardNumber, BigDecimal amount) throws PaymentException;
}
