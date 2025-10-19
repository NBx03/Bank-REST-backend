package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO с информацией о банковской карте.
 */
public record CardDto(
        Long id,
        String lastDigits,
        LocalDate expirationDate,
        CardStatus status,
        BigDecimal balance
) {
}