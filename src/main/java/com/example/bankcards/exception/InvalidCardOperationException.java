package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, выбрасываемое при некорректных операциях с картами.
 */
@StandardException
public class InvalidCardOperationException extends BankcardsException {
}