package com.example.bankcards.exception;

/**
 * Исключение в случае попытки создать дубликат данных.
 */
public class DuplicateResourceException extends BankcardsException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}