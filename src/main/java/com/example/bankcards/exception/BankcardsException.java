package com.example.bankcards.exception;

/**
 * Базовое непроверяемое исключение для приложения.
 */
public class BankcardsException extends RuntimeException {

    public BankcardsException(String message) {
        super(message);
    }

    public BankcardsException(String message, Throwable cause) {
        super(message, cause);
    }
}