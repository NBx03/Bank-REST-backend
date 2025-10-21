package com.example.bankcards.exception;

import java.math.BigDecimal;
import lombok.Getter;

/**
 * Исключение превышения лимита переводов.
 */
@Getter
public class TransferLimitExceededException extends BankcardsException {

    private final BigDecimal allowedLimit;

    public TransferLimitExceededException(String message, BigDecimal allowedLimit) {
        super(message);
        this.allowedLimit = allowedLimit;
    }

}