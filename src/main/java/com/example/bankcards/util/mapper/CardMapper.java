package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardNumberEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardMapper {

    private final CardNumberEncoder cardNumberEncoder;

    public CardDto toDto(Card card) {
        if (card == null) {
            return null;
        }
        String maskedNumber = null;
        if (card.getEncryptedNumber() != null) {
            String decrypted = cardNumberEncoder.decrypt(card.getEncryptedNumber());
            maskedNumber = cardNumberEncoder.mask(decrypted);
        }
        return new CardDto(
                card.getId(),
                maskedNumber,
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }
}