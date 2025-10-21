package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.dto.UpdateUserRolesRequestDto;
import com.example.bankcards.dto.UpdateUserStatusRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления пользователями.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Регистрирует нового пользователя.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerUser(@Valid @RequestBody CreateUserRequestDto request) {
        return userService.registerUser(request);
    }

    /**
     * Возвращает пользователя по ID.
     */
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    /**
     * Возвращает список всех пользователей.
     */
    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    /**
     * Обновляет статус пользователя.
     */
    @PatchMapping("/{id}/status")
    public UserDto updateStatus(@PathVariable Long id,
                                @Valid @RequestBody UpdateUserStatusRequestDto request) {
        return userService.updateStatus(id, request.status());
    }

    /**
     * Обновляет набор ролей пользователя.
     */
    @PutMapping("/{id}/roles")
    public UserDto updateRoles(@PathVariable Long id,
                               @Valid @RequestBody UpdateUserRolesRequestDto request) {
        return userService.assignRoles(id, request.roles());
    }
}