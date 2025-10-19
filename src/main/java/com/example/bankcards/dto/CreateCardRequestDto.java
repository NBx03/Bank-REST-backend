package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Запрос на выпуск новой банковской карты пользователю.
 */
public record CreateCardRequestDto(
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{16}", message = "Card number must contain exactly 16 digits")
        String cardNumber,

        @NotNull(message = "Expiration date is required")
        @Future(message = "Expiration date must be in the future")
        LocalDate expirationDate,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Initial balance must not be negative")
        @Digits(integer = 17, fraction = 2, message = "Amount must have up to 2 fractional digits")
        BigDecimal initialBalance
) {
}