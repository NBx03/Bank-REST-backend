package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByStatus(UserStatus status);
}