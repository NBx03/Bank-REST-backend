package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.LoginRequestDto;
import com.example.bankcards.dto.auth.RefreshTokenRequestDto;
import com.example.bankcards.security.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для аутентификации.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public JwtResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authenticationService.authenticate(request);
    }

    @PostMapping("/refresh")
    public JwtResponseDto refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return authenticationService.refreshToken(request);
    }
}