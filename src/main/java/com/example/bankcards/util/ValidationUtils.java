package com.example.bankcards.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Вспомогательные методы для валидации входных данных.
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    public static BigDecimal normalizeAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount must be provided");
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalized.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return normalized;
    }
}