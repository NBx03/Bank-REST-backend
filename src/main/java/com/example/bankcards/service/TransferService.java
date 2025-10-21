package com.example.bankcards.service;

import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.dto.CardTransferRequestDto;
import java.util.List;

/**
 * Сервис для обработки переводов между картами.
 */
public interface TransferService {

    CardTransferDto transfer(Long operatorId, CardTransferRequestDto request);

    List<CardTransferDto> getTransfersForCard(Long operatorId, Long cardId);
}