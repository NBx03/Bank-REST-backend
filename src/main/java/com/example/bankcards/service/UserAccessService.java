package com.example.bankcards.service;

import com.example.bankcards.entity.User;

/**
 * Сервис для проверки прав доступа и статуса пользователя.
 */
public interface UserAccessService {

    User requireActiveUser(Long userId);

    boolean isAdmin(User user);

    void ensureUserRole(User user);
}