package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UserInactiveException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserAccessService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccessServiceImpl implements UserAccessService {

    private final UserRepository userRepository;

    @Override
    public User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException("User " + userId + " is not active");
        }
        return user;
    }

    @Override
    public boolean isAdmin(User user) {
        return hasRole(user.getRoles(), RoleType.ADMIN);
    }

    @Override
    public void ensureUserRole(User user) {
        if (!hasRole(user.getRoles(), RoleType.USER)) {
            throw new AccessDeniedException("User " + user.getId() + " must have USER role for this operation");
        }
    }

    private boolean hasRole(Set<Role> roles, RoleType roleType) {
        return roles.stream().anyMatch(role -> role.getName() == roleType);
    }
}