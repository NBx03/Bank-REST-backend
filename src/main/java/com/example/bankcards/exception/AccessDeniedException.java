package com.example.bankcards.exception;

/**
 * Исключение, возникающее при попытке выполнить действие без достаточных прав.
 */
public class AccessDeniedException extends BankcardsException {

    public AccessDeniedException(String message) {
        super(message);
    }
}