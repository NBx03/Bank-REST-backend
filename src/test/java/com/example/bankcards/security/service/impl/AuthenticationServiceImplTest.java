package com.example.bankcards.security.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.LoginRequestDto;
import com.example.bankcards.dto.auth.RefreshTokenRequestDto;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import com.example.bankcards.security.model.UserPrincipal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new UserPrincipal(1L, "john.doe", "encoded", List.of(), true, true, true, true);
    }

    @Test
    void authenticate_shouldReturnJwtResponse() {
        LoginRequestDto request = new LoginRequestDto("john.doe", "Secret123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.createAccessToken(principal)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(principal)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(jwtTokenProvider.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));

        JwtResponseDto response = authenticationService.authenticate(request);

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(jwtTokenProvider).createAccessToken(principal);
        verify(jwtTokenProvider).createRefreshToken(principal);
    }

    @Test
    void refreshToken_shouldReturnNewTokens() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("refresh-token");
        when(jwtTokenProvider.getUserFromRefreshToken(request.refreshToken())).thenReturn(principal);
        when(jwtTokenProvider.createAccessToken(principal)).thenReturn("new-access");
        when(jwtTokenProvider.createRefreshToken(principal)).thenReturn("new-refresh");
        when(jwtTokenProvider.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(jwtTokenProvider.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));

        JwtResponseDto response = authenticationService.refreshToken(request);

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        verify(jwtTokenProvider).getUserFromRefreshToken(request.refreshToken());
    }
}