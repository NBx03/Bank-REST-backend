package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на обновление статуса карты.
 */
public record UpdateCardStatusRequestDto(
        @NotNull(message = "Status is required")
        CardStatus status
) {
}