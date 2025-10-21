package com.example.bankcards.dto.auth;

/**
 * Ответ с access и refresh-токенами.
 */
public record JwtResponseDto(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresIn,
        long refreshExpiresIn
) {
}