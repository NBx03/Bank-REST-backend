package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Запрос на регистрацию нового пользователя.
 */
public record CreateUserRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must be 100 characters or fewer")
        String email,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must contain from 3 to 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must contain from 8 to 255 characters")
        String password,

        @Size(max = 100, message = "First name must be 100 characters or fewer")
        String firstName,

        @Size(max = 100, message = "Last name must be 100 characters or fewer")
        String lastName,

        @NotEmpty(message = "At least one role must be specified")
        Set<RoleType> roles
) {
}