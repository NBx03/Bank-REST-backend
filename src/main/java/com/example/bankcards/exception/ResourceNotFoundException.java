package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, выбрасываемое когда сущность не найдена.
 */
@StandardException
public class ResourceNotFoundException extends BankcardsException {
}