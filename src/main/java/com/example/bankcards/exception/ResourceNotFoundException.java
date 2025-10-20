package com.example.bankcards.exception;

/**
 * Исключение, выбрасываемое когда сущность не найдена.
 */
public class ResourceNotFoundException extends BankcardsException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}