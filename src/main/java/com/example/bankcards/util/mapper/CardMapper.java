package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardDto toDto(Card card) {
        if (card == null) {
            return null;
        }
        return new CardDto(
                card.getId(),
                card.getLastDigits(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }
}