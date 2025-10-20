package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import java.util.List;

/**
 * Сервис для управления банковскими картами.
 */
public interface CardService {

    CardDto issueCard(Long userId, CreateCardRequestDto request);

    CardDto getCard(Long cardId);

    List<CardDto> getUserCards(Long userId);

    CardDto changeStatus(Long cardId, CardStatus newStatus);
}