package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, возникающее при попытке выполнить действие без достаточных прав.
 */
@StandardException
public class AccessDeniedException extends BankcardsException {
}