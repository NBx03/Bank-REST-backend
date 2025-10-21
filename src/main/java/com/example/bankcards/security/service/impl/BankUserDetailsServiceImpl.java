package com.example.bankcards.security.service.impl;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.model.UserPrincipal;
import com.example.bankcards.security.service.BankUserDetailsService;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankUserDetailsServiceImpl implements BankUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(username);
        }
        User user = userOptional
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return toPrincipal(user);
    }

    @Override
    public UserPrincipal loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return toPrincipal(user);
    }

    private UserPrincipal toPrincipal(User user) {
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(this::toAuthority)
                .collect(Collectors.toUnmodifiableSet());

        UserStatus status = user.getStatus();
        boolean accountNonLocked = status != UserStatus.BLOCKED;
        boolean enabled = status == UserStatus.ACTIVE;
        boolean accountNonExpired = status != UserStatus.ARCHIVED;

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                accountNonExpired,
                accountNonLocked,
                true,
                enabled
        );
    }

    private SimpleGrantedAuthority toAuthority(RoleType role) {
        return new SimpleGrantedAuthority("ROLE_" + role.name());
    }
}