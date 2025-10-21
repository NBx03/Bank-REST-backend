package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, выбрасываемое при обращении к заблокированным или архивированным пользователям.
 */
@StandardException
public class UserInactiveException extends BankcardsException {
}