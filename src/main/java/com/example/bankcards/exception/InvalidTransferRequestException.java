package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение при некорректных параметрах перевода.
 */
@StandardException
public class InvalidTransferRequestException extends BankcardsException {
}