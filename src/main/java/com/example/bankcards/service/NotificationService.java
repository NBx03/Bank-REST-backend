package com.example.bankcards.service;

import com.example.bankcards.entity.CardTransfer;

/**
 * Сервис внешних уведомлений.
 */
public interface NotificationService {

    void notifyTransfer(CardTransfer transfer);
}