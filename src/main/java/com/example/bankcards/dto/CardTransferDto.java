package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.TransferStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO перевода между картами.
 */
public record CardTransferDto(
        Long id,
        Long fromCardId,
        Long toCardId,
        BigDecimal amount,
        TransferStatus status,
        String description,
        LocalDateTime createdAt
) {
}