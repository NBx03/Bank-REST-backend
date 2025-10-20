package com.example.bankcards.exception;

/**
 * Исключение при некорректных параметрах перевода.
 */
public class InvalidTransferRequestException extends BankcardsException {

    public InvalidTransferRequestException(String message) {
        super(message);
    }
}