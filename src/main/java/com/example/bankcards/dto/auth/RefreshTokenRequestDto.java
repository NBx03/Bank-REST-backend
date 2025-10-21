package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на обновление пары JWT токенов.
 */
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}