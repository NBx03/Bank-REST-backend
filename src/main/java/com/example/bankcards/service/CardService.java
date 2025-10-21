package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.UpdateCardRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис для управления банковскими картами.
 */
public interface CardService {

    CardDto issueCard(Long operatorId, Long userId, CreateCardRequestDto request);

    CardDto getCard(Long operatorId, Long cardId);

    Page<CardDto> getUserCards(Long operatorId, Long userId, CardStatus status, Pageable pageable);

    CardDto changeStatus(Long operatorId, Long cardId, CardStatus newStatus);

    CardDto updateCard(Long operatorId, Long cardId, UpdateCardRequestDto request);

    void deleteCard(Long operatorId, Long cardId);
}