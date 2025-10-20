package com.example.bankcards.exception;

import java.math.BigDecimal;

/**
 * Исключение превышения лимита переводов.
 */
public class TransferLimitExceededException extends BankcardsException {

    private final BigDecimal allowedLimit;

    public TransferLimitExceededException(String message, BigDecimal allowedLimit) {
        super(message);
        this.allowedLimit = allowedLimit;
    }

    public BigDecimal getAllowedLimit() {
        return allowedLimit;
    }
}