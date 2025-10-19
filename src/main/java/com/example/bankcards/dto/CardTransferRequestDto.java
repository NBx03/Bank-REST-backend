package com.example.bankcards.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Запрос на перевод между банковскими картами.
 */
public record CardTransferRequestDto(
        @NotBlank(message = "Source card number is required")
        @Pattern(regexp = "\\d{16}", message = "Source card number must contain exactly 16 digits")
        String fromCardNumber,

        @NotBlank(message = "Target card number is required")
        @Pattern(regexp = "\\d{16}", message = "Target card number must contain exactly 16 digits")
        String toCardNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        @Digits(integer = 17, fraction = 2, message = "Amount must have up to 2 fractional digits")
        BigDecimal amount,

        @Size(max = 255, message = "Description must be 255 characters or fewer")
        String description
) {
}