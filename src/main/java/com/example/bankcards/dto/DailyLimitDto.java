package com.example.bankcards.dto;

import java.math.BigDecimal;

/**
 * Информация об остатке суточного лимита карты.
 */
public record DailyLimitDto(BigDecimal remaining) {
}