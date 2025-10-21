package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на обновление статуса пользователя.
 */
public record UpdateUserStatusRequestDto(
        @NotNull(message = "Status is required")
        UserStatus status
) {
}