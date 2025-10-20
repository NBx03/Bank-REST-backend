package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardInactiveException;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.util.CardNumberEncoder;
import jakarta.transaction.Transactional;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CardNumberEncoder cardNumberEncoder;

    public CardServiceImpl(CardRepository cardRepository,
                           UserRepository userRepository,
                           CardMapper cardMapper,
                           CardNumberEncoder cardNumberEncoder) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardMapper = cardMapper;
        this.cardNumberEncoder = cardNumberEncoder;
    }

    @Override
    public CardDto issueCard(Long userId, CreateCardRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String normalizedNumber = normalizeCardNumber(request.cardNumber());
        String encrypted = cardNumberEncoder.encrypt(normalizedNumber);
        cardRepository.findByEncryptedNumber(encrypted)
                .ifPresent(card -> {
                    throw new DuplicateResourceException("Card already exists for provided number");
                });

        Card card = new Card();
        card.setOwner(user);
        card.setEncryptedNumber(encrypted);
        card.setLastDigits(cardNumberEncoder.extractLastDigits(normalizedNumber));
        card.setExpirationDate(request.expirationDate());
        card.setBalance(request.initialBalance().setScale(2, RoundingMode.HALF_UP));
        card.setStatus(CardStatus.ACTIVE);

        Card saved = cardRepository.save(card);
        return cardMapper.toDto(saved);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public CardDto getCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<CardDto> getUserCards(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return cardRepository.findAllByOwnerId(userId).stream()
                .map(cardMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CardDto changeStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
        if (card.getStatus() == newStatus) {
            return cardMapper.toDto(card);
        }
        if (card.getStatus() == CardStatus.BLOCKED && newStatus == CardStatus.CLOSED) {
            throw new CardInactiveException("Blocked card cannot be directly closed");
        }
        card.setStatus(newStatus);
        cardRepository.save(card);
        return cardMapper.toDto(card);
    }

    private String normalizeCardNumber(String cardNumber) {
        if (!StringUtils.hasText(cardNumber)) {
            throw new IllegalArgumentException("Card number must not be empty");
        }
        return cardNumber.replaceAll("\\s", "");
    }
}