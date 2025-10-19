package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByEncryptedNumber(String encryptedNumber);

    List<Card> findAllByOwnerId(Long ownerId);

    List<Card> findAllByOwnerIdAndStatus(Long ownerId, CardStatus status);
}