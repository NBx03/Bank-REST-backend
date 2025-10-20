package com.example.bankcards.exception;

/**
 * Исключение, выбрасываемое при попытке операции с неактивной картой.
 */
public class CardInactiveException extends BankcardsException {

    public CardInactiveException(String message) {
        super(message);
    }
}