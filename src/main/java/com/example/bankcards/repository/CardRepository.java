package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByEncryptedNumber(String encryptedNumber);

    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    Page<Card> findAllByOwnerIdAndStatus(Long ownerId, CardStatus status, Pageable pageable);
}