package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import java.util.List;

/**
 * Сервис для управления банковскими картами.
 */
public interface CardService {

    CardDto issueCard(Long operatorId, Long userId, CreateCardRequestDto request);

    CardDto getCard(Long operatorId, Long cardId);

    List<CardDto> getUserCards(Long operatorId, Long userId);

    CardDto changeStatus(Long operatorId, Long cardId, CardStatus newStatus);
}