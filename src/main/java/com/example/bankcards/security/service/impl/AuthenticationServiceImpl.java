package com.example.bankcards.security.service.impl;

import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.LoginRequestDto;
import com.example.bankcards.dto.auth.RefreshTokenRequestDto;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import com.example.bankcards.security.model.UserPrincipal;
import com.example.bankcards.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public JwtResponseDto authenticate(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.createAccessToken(principal);
        String refreshToken = jwtTokenProvider.createRefreshToken(principal);
        return buildResponse(accessToken, refreshToken);
    }

    @Override
    public JwtResponseDto refreshToken(RefreshTokenRequestDto request) {
        UserPrincipal principal = jwtTokenProvider.getUserFromRefreshToken(request.refreshToken());
        String accessToken = jwtTokenProvider.createAccessToken(principal);
        String refreshToken = jwtTokenProvider.createRefreshToken(principal);
        return buildResponse(accessToken, refreshToken);
    }

    private JwtResponseDto buildResponse(String accessToken, String refreshToken) {
        return new JwtResponseDto(
                TOKEN_TYPE,
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenTtl().toSeconds(),
                jwtTokenProvider.getRefreshTokenTtl().toSeconds()
        );
    }
}