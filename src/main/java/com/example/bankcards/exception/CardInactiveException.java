package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, выбрасываемое при попытке операции с неактивной картой.
 */
@StandardException
public class CardInactiveException extends BankcardsException {
}