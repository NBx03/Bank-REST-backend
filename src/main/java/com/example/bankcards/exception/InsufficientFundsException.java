package com.example.bankcards.exception;

/**
 * Исключение при недостатке средств на карте.
 */
public class InsufficientFundsException extends BankcardsException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}