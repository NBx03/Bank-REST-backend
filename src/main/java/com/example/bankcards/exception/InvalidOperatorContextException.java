package com.example.bankcards.exception;

/**
 * Исключение, сигнализирующее о проблемах с идентификатором оператора в запросе.
 */
public class InvalidOperatorContextException extends BankcardsException {

    public InvalidOperatorContextException(String message) {
        super(message);
    }

    public InvalidOperatorContextException(String message, Throwable cause) {
        super(message, cause);
    }
}