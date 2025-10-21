package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.UserMapper;
import jakarta.transaction.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Реализация {@link UserService}.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto registerUser(CreateUserRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User with email already exists");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User with username already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setStatus(UserStatus.ACTIVE);

        Set<Role> roles = resolveRoles(request.roles());
        roles.forEach(user::addRole);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setStatus(status);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto assignRoles(Long userId, Set<RoleType> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Set<Role> resolvedRoles = resolveRoles(roles);
        // Очистка текущих ролей через вспомогательные методы, чтобы корректно обновить связь.
        new LinkedHashSet<>(user.getRoles()).forEach(user::removeRole);
        resolvedRoles.forEach(user::addRole);
        return userMapper.toDto(user);
    }

    private Set<Role> resolveRoles(Set<RoleType> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(this::findOrCreateRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Role findOrCreateRole(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseGet(() -> roleRepository.save(new Role(roleType)));
    }
}