package com.example.bankcards.service;

import java.math.BigDecimal;

/**
 * Сервис проверки лимитов переводов.
 */
public interface TransferLimitService {

    void validateDailyLimit(Long cardId, BigDecimal amount);

    BigDecimal getRemainingDailyLimit(Long cardId);
}