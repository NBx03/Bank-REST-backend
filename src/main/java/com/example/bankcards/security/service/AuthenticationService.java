package com.example.bankcards.security.service;

import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.LoginRequestDto;
import com.example.bankcards.dto.auth.RefreshTokenRequestDto;

/**
 * Сервис аутентификации и обновления JWT токенов.
 */
public interface AuthenticationService {

    JwtResponseDto authenticate(LoginRequestDto request);

    JwtResponseDto refreshToken(RefreshTokenRequestDto request);
}