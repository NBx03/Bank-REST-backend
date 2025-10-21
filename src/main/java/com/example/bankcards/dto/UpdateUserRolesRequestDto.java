package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.RoleType;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * Запрос на обновление набора ролей пользователя.
 */
public record UpdateUserRolesRequestDto(
        @NotEmpty(message = "At least one role must be specified")
        Set<RoleType> roles
) {
}