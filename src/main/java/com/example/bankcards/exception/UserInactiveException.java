package com.example.bankcards.exception;

/**
 * Исключение, выбрасываемое при обращении к заблокированным или архивированным пользователям.
 */
public class UserInactiveException extends BankcardsException {

    public UserInactiveException(String message) {
        super(message);
    }
}