package com.example.bankcards.testutil;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.dto.DailyLimitDto;
import com.example.bankcards.dto.UpdateCardStatusRequestDto;
import com.example.bankcards.dto.UpdateUserRolesRequestDto;
import com.example.bankcards.dto.UpdateUserStatusRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.LoginRequestDto;
import com.example.bankcards.dto.auth.RefreshTokenRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.entity.enums.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Утилиты для подготовки типичных DTO в тестах.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static CreateUserRequestDto createUserRequest() {
        return new CreateUserRequestDto(
                "john.doe@example.com",
                "john.doe",
                "Secret123",
                "John",
                "Doe",
                Set.of(RoleType.USER)
        );
    }

    public static UserDto createUserDto(Long id) {
        return new UserDto(
                id,
                "john.doe@example.com",
                "john.doe",
                "John",
                "Doe",
                UserStatus.ACTIVE,
                Set.of(RoleType.USER)
        );
    }

    public static UpdateUserStatusRequestDto updateUserStatusRequest(UserStatus status) {
        return new UpdateUserStatusRequestDto(status);
    }

    public static UpdateUserRolesRequestDto updateUserRolesRequest(Set<RoleType> roles) {
        return new UpdateUserRolesRequestDto(roles);
    }

    public static CreateCardRequestDto createCardRequest() {
        return new CreateCardRequestDto(
                "4111222233334444",
                LocalDate.now().plusYears(2),
                BigDecimal.valueOf(1000)
        );
    }

    public static UpdateCardStatusRequestDto updateCardStatusRequest(CardStatus status) {
        return new UpdateCardStatusRequestDto(status);
    }

    public static CardDto cardDto(Long id) {
        return new CardDto(id, "4444", LocalDate.now().plusYears(2), CardStatus.ACTIVE, BigDecimal.valueOf(1000));
    }

    public static CardTransferRequestDto transferRequest() {
        return new CardTransferRequestDto(
                "4111222233334444",
                "5555666677778888",
                BigDecimal.valueOf(1500),
                "Rent payment"
        );
    }

    public static CardTransferDto cardTransferDto(Long id) {
        return new CardTransferDto(
                id,
                10L,
                20L,
                BigDecimal.valueOf(1500),
                TransferStatus.COMPLETED,
                "Rent payment",
                LocalDateTime.now()
        );
    }

    public static DailyLimitDto dailyLimit(BigDecimal remaining) {
        return new DailyLimitDto(remaining);
    }

    public static LoginRequestDto loginRequest() {
        return new LoginRequestDto("john.doe", "Secret123");
    }

    public static RefreshTokenRequestDto refreshTokenRequest() {
        return new RefreshTokenRequestDto("refresh-token");
    }

    public static JwtResponseDto jwtResponse() {
        return new JwtResponseDto("Bearer", "access", "refresh", 900L, 604800L);
    }

    public static List<CardDto> cardList() {
        return List.of(cardDto(1L));
    }

    public static List<CardTransferDto> transferList() {
        return List.of(cardTransferDto(1L));
    }
}