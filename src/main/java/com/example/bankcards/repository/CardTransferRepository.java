package com.example.bankcards.repository;

import com.example.bankcards.entity.CardTransfer;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTransferRepository extends JpaRepository<CardTransfer, Long> {

    List<CardTransfer> findAllByFromCardId(Long fromCardId);

    List<CardTransfer> findAllByToCardId(Long toCardId);

    List<CardTransfer> findAllByFromCardIdAndCreatedAtBetween(Long fromCardId, LocalDateTime from, LocalDateTime to);

    boolean existsByFromCardIdOrToCardId(Long fromCardId, Long toCardId);
}