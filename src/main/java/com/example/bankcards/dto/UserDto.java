package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.UserStatus;
import java.util.Set;

/**
 * DTO с информацией о пользователе.
 */
public record UserDto(
        Long id,
        String email,
        String username,
        String firstName,
        String lastName,
        UserStatus status,
        Set<RoleType> roles
) {
}