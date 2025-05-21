package com.playtomic.tests.wallet.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * Adapter to delegate payment processing to the existing StripeService.
 * Translates StripeServiceException into PaymentException.
 */
@Component
public class StripePaymentGateway implements PaymentGateway {

    private final StripeService stripeService;

    public StripePaymentGateway(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @Override
    public void charge(String creditCardNumber, BigDecimal amount) {
        try {
            stripeService.charge(creditCardNumber, amount);
        } catch (StripeAmountTooSmallException e) {
            // Translate specific Stripe exception to generic domain exception
            throw new PaymentException("Amount too small", e);
        } catch (StripeServiceException e) {
            throw new PaymentException("Payment failed", e);
        }
    }
}
