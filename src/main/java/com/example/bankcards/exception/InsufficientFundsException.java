package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение при недостатке средств на карте.
 */
@StandardException
public class InsufficientFundsException extends BankcardsException {
}