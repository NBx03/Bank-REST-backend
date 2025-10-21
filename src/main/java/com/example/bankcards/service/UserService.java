package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.UserStatus;
import java.util.List;
import java.util.Set;

/**
 * Сервис управления пользователями и их ролями.
 */
public interface UserService {

    UserDto registerUser(CreateUserRequestDto request);

    UserDto getUser(Long userId);

    List<UserDto> getUsers();

    UserDto updateStatus(Long userId, UserStatus status);

    UserDto assignRoles(Long userId, Set<RoleType> roles);
}