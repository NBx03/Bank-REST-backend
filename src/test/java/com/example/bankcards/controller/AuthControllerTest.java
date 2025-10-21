package com.example.bankcards.controller;

import static com.example.bankcards.testutil.TestDataFactory.jwtResponse;
import static com.example.bankcards.testutil.TestDataFactory.loginRequest;
import static com.example.bankcards.testutil.TestDataFactory.refreshTokenRequest;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.LoginRequestDto;
import com.example.bankcards.dto.auth.RefreshTokenRequestDto;
import com.example.bankcards.security.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<LoginRequestDto> loginRequestCaptor;

    @Test
    void login_shouldReturnTokensForValidCredentials() throws Exception {
        LoginRequestDto request = loginRequest();
        JwtResponseDto response = jwtResponse();
        when(authenticationService.authenticate(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType", equalTo("Bearer")))
                .andExpect(jsonPath("$.accessToken", equalTo("access")));

        verify(authenticationService).authenticate(loginRequestCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(loginRequestCaptor.getValue()).isEqualTo(request);
    }

    @Test
    void login_shouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Validation failed")))
                .andExpect(jsonPath("$.validationErrors", hasSize(1)));

        verify(authenticationService, never()).authenticate(any());
    }

    @Test
    void refresh_shouldReturnTokens() throws Exception {
        RefreshTokenRequestDto request = refreshTokenRequest();
        JwtResponseDto response = jwtResponse();
        when(authenticationService.refreshToken(any(RefreshTokenRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken", equalTo("refresh")));
    }
}