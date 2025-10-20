package com.example.bankcards.service;

import com.example.bankcards.entity.Card;

/**
 * Сервис управления статусами карт в зависимости от срока действия.
 */
public interface CardLifecycleService {
    Card refreshExpiration(Card card);
}