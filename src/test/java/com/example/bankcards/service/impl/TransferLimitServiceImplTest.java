package com.example.bankcards.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bankcards.config.properties.TransferProperties;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.exception.TransferLimitExceededException;
import com.example.bankcards.repository.CardTransferRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferLimitServiceImplTest {

    @Mock
    private CardTransferRepository cardTransferRepository;

    private TransferProperties transferProperties;

    @InjectMocks
    private TransferLimitServiceImpl transferLimitService;

    @BeforeEach
    void setUp() {
        transferProperties = new TransferProperties();
        transferLimitService = new TransferLimitServiceImpl(cardTransferRepository, transferProperties);
    }

    @Test
    void validateDailyLimit_shouldPassWhenLimitNotExceeded() {
        mockTransfers(BigDecimal.valueOf(200), TransferStatus.COMPLETED);
        transferProperties.getLimit().setDaily(BigDecimal.valueOf(500));

        transferLimitService.validateDailyLimit(1L, BigDecimal.valueOf(100));

        verify(cardTransferRepository).findAllByFromCardIdAndCreatedAtBetween(eq(1L), any(), any());
    }

    @Test
    void validateDailyLimit_shouldThrowWhenLimitExceeded() {
        mockTransfers(BigDecimal.valueOf(400), TransferStatus.COMPLETED);
        transferProperties.getLimit().setDaily(BigDecimal.valueOf(500));

        assertThatThrownBy(() -> transferLimitService.validateDailyLimit(1L, BigDecimal.valueOf(200)))
                .isInstanceOf(TransferLimitExceededException.class)
                .hasMessageContaining("Daily transfer limit of 500");
    }

    @Test
    void validateDailyLimit_shouldIgnoreDisabledLimit() {
        transferProperties.getLimit().setDaily(null);

        transferLimitService.validateDailyLimit(1L, BigDecimal.valueOf(100));

        verify(cardTransferRepository, never()).findAllByFromCardIdAndCreatedAtBetween(any(), any(), any());
    }

    @Test
    void getRemainingDailyLimit_shouldReturnPositiveValue() {
        mockTransfers(BigDecimal.valueOf(120), TransferStatus.PENDING);
        transferProperties.getLimit().setDaily(BigDecimal.valueOf(500));

        BigDecimal remaining = transferLimitService.getRemainingDailyLimit(1L);

        assertThat(remaining).isEqualByComparingTo("380");
    }

    @Test
    void getRemainingDailyLimit_shouldNotGoBelowZero() {
        mockTransfers(BigDecimal.valueOf(700), TransferStatus.COMPLETED);
        transferProperties.getLimit().setDaily(BigDecimal.valueOf(500));

        BigDecimal remaining = transferLimitService.getRemainingDailyLimit(1L);

        assertThat(remaining).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private void mockTransfers(BigDecimal amount, TransferStatus status) {
        CardTransfer transfer = new CardTransfer();
        transfer.setAmount(amount);
        transfer.setStatus(status);
        when(cardTransferRepository.findAllByFromCardIdAndCreatedAtBetween(eq(1L), any(), any()))
                .thenReturn(List.of(transfer));
    }
}