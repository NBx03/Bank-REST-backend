package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByStatus(UserStatus status);
}