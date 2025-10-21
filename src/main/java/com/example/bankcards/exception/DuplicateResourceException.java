package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение в случае попытки создать дубликат данных.
 */
@StandardException
public class DuplicateResourceException extends BankcardsException {
}