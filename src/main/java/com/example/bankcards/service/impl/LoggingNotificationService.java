package com.example.bankcards.service.impl;

import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Простая реализация уведомлений через логирование.
 */
@Service
public class LoggingNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationService.class);

    @Override
    public void notifyTransfer(CardTransfer transfer) {
        log.info("Notify transfer id={} amount={} from={} to={}",
                transfer.getId(),
                transfer.getAmount(),
                transfer.getFromCard().getId(),
                transfer.getToCard().getId());
    }
}