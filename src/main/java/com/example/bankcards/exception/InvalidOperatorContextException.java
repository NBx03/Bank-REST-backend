package com.example.bankcards.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, сигнализирующее о проблемах с ID оператора в запросе.
 */
@StandardException
public class InvalidOperatorContextException extends BankcardsException {
}