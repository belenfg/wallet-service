package com.playtomic.tests.wallet.service;

/**
 * Generic payment processing exception in the domain.
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
    public PaymentException(String message) {
        super(message);
    }
}
