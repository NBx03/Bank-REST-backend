package com.example.bankcards.security.service;

import com.example.bankcards.security.model.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Сервис загрузки пользователей для Spring Security.
 */
public interface BankUserDetailsService extends UserDetailsService {

    UserPrincipal loadUserById(Long userId);
}