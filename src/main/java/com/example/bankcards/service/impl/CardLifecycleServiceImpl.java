package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardLifecycleService;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class CardLifecycleServiceImpl implements CardLifecycleService {

    private final CardRepository cardRepository;

    public CardLifecycleServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public Card refreshExpiration(Card card) {
        if (card == null) {
            return null;
        }
        if (card.getStatus() == CardStatus.CLOSED || card.getStatus() == CardStatus.EXPIRED) {
            return card;
        }
        LocalDate expirationDate = card.getExpirationDate();
        if (expirationDate == null) {
            return card;
        }
        LocalDate today = LocalDate.now();
        if (!expirationDate.isAfter(today)) {
            card.setStatus(CardStatus.EXPIRED);
            return cardRepository.save(card);
        }
        return card;
    }
}
