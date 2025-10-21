package com.example.bankcards.service.impl;

import com.example.bankcards.config.properties.TransferProperties;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.exception.TransferLimitExceededException;
import com.example.bankcards.repository.CardTransferRepository;
import com.example.bankcards.service.TransferLimitService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional(Transactional.TxType.SUPPORTS)
@RequiredArgsConstructor
public class TransferLimitServiceImpl implements TransferLimitService {

    private static final Set<TransferStatus> STATUSES_FOR_LIMIT = EnumSet.of(
            TransferStatus.PENDING,
            TransferStatus.COMPLETED
    );

    private final CardTransferRepository cardTransferRepository;
    private final TransferProperties transferProperties;

    @Override
    public void validateDailyLimit(Long cardId, BigDecimal amount) {
        BigDecimal limit = transferProperties.getLimit().getDaily();
        if (limit == null) {
            return;
        }
        BigDecimal spent = calculateDailySpent(cardId);
        if (spent.add(amount).compareTo(limit) > 0) {
            String message = String.format("Daily transfer limit of %s exceeded", limit);
            throw new TransferLimitExceededException(message, limit);
        }
    }

    @Override
    public BigDecimal getRemainingDailyLimit(Long cardId) {
        BigDecimal limit = transferProperties.getLimit().getDaily();
        if (limit == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal spent = calculateDailySpent(cardId);
        BigDecimal remaining = limit.subtract(spent);
        return remaining.max(BigDecimal.ZERO);
    }

    private BigDecimal calculateDailySpent(Long cardId) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        List<CardTransfer> transfers = cardTransferRepository
                .findAllByFromCardIdAndCreatedAtBetween(cardId, from, to);
        return transfers.stream()
                .filter(transfer -> STATUSES_FOR_LIMIT.contains(transfer.getStatus()))
                .map(CardTransfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}