package com.example.bankcards.util;

import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.entity.CardTransfer;
import org.springframework.stereotype.Component;

@Component
public class CardTransferMapper {

    public CardTransferDto toDto(CardTransfer transfer) {
        if (transfer == null) {
            return null;
        }
        return new CardTransferDto(
                transfer.getId(),
                transfer.getFromCard().getId(),
                transfer.getToCard().getId(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getDescription(),
                transfer.getCreatedAt()
        );
    }
}