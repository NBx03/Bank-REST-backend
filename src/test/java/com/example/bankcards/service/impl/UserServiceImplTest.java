package com.example.bankcards.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bankcards.dto.CreateUserRequestDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(RoleType.USER);
        userRole.setId(1L);
    }

    @Test
    void registerUser_shouldCreateUserAndReturnDto() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "john.doe@example.com",
                "john.doe",
                "Secret123",
                "John",
                "Doe",
                Set.of(RoleType.USER)
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(roleRepository.findByName(RoleType.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });
        UserDto expectedDto = new UserDto(
                42L,
                request.email(),
                request.username(),
                request.firstName(),
                request.lastName(),
                UserStatus.ACTIVE,
                Set.of(RoleType.USER)
        );
        when(userMapper.toDto(any(User.class))).thenReturn(expectedDto);

        UserDto result = userService.registerUser(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(request.email());
        assertThat(savedUser.getRoles()).containsExactly(userRole);
    }

    @Test
    void registerUser_shouldThrowWhenEmailExists() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "john.doe@example.com",
                "john.doe",
                "Secret123",
                "John",
                "Doe",
                Set.of(RoleType.USER)
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_shouldThrowWhenUsernameExists() {
        CreateUserRequestDto request = new CreateUserRequestDto(
                "john.doe@example.com",
                "john.doe",
                "Secret123",
                "John",
                "Doe",
                Set.of(RoleType.USER)
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUser_shouldReturnDto() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        UserDto expectedDto = new UserDto(10L, "john.doe@example.com", "john", null, null, UserStatus.ACTIVE, Set.of());
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserDto result = userService.getUser(10L);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void getUser_shouldThrowWhenNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUsers_shouldReturnMappedDtos() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(user));
        UserDto dto = new UserDto(1L, "john.doe@example.com", "john", null, null, UserStatus.ACTIVE, Set.of());
        when(userMapper.toDto(user)).thenReturn(dto);

        List<UserDto> users = userService.getUsers();

        assertThat(users).containsExactly(dto);
    }

    @Test
    void updateStatus_shouldPropagateStatusToCards() {
        User user = new User();
        user.setId(1L);
        Card activeCard = new Card();
        activeCard.setId(10L);
        activeCard.setStatus(CardStatus.ACTIVE);
        activeCard.setOwner(user);
        Card blockedCard = new Card();
        blockedCard.setId(11L);
        blockedCard.setStatus(CardStatus.BLOCKED);
        blockedCard.setOwner(user);
        user.addCard(activeCard);
        user.addCard(blockedCard);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDto dto = new UserDto(1L, "john.doe@example.com", "john", null, null, UserStatus.BLOCKED, Set.of());
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.updateStatus(1L, UserStatus.BLOCKED);

        assertThat(result).isEqualTo(dto);
        assertThat(activeCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(blockedCard.getStatus()).isEqualTo(CardStatus.BLOCKED);

        user.removeCard(blockedCard);

        assertThat(user.getCards()).containsExactlyInAnyOrder(activeCard);
        assertThat(blockedCard.getOwner()).isNull();
    }

    @Test
    void assignRoles_shouldReplaceExistingRoles() {
        Role adminRole = new Role(RoleType.ADMIN);
        adminRole.setId(2L);
        User user = new User();
        user.setId(1L);
        user.addRole(userRole);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleType.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userMapper.toDto(user)).thenReturn(new UserDto(1L, "john.doe@example.com", "john", null, null, UserStatus.ACTIVE, Set.of(RoleType.ADMIN)));

        UserDto result = userService.assignRoles(1L, Set.of(RoleType.ADMIN));

        assertThat(result.roles()).containsExactly(RoleType.ADMIN);
        assertThat(user.getRoles()).containsExactly(adminRole);
    }

    @Test
    void assignRoles_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRoles(1L, Set.of(RoleType.ADMIN)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}