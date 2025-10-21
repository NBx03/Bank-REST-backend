package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на аутентификацию пользователя.
 */
public record LoginRequestDto(
        @NotBlank(message = "Username is required")
        @Size(max = 100, message = "Username must be 100 characters or fewer")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must contain from 8 to 255 characters")
        String password
) {
}