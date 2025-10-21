package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardInactiveException;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UserInactiveException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardLifecycleService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserAccessService;
import com.example.bankcards.util.mapper.CardMapper;
import com.example.bankcards.util.CardNumberEncoder;
import jakarta.transaction.Transactional;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CardNumberEncoder cardNumberEncoder;
    private final CardLifecycleService cardLifecycleService;
    private final UserAccessService userAccessService;

    @Override
    public CardDto issueCard(Long operatorId, Long userId, CreateCardRequestDto request) {
        User operator = userAccessService.requireActiveUser(operatorId);
        ensureCanManageUser(operator, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException("User " + userId + " is not active");
        }

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
    public CardDto getCard(Long operatorId, Long cardId) {
        User operator = userAccessService.requireActiveUser(operatorId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
        ensureCanViewCard(operator, card);
        Card updated = cardLifecycleService.refreshExpiration(card);
        return cardMapper.toDto(updated);
    }

    @Override
    public List<CardDto> getUserCards(Long operatorId, Long userId) {
        User operator = userAccessService.requireActiveUser(operatorId);
        ensureCanManageUser(operator, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException("User " + userId + " is not active");
        }
        return cardRepository.findAllByOwnerId(user.getId()).stream()
                .map(cardLifecycleService::refreshExpiration)
                .map(cardMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CardDto changeStatus(Long operatorId, Long cardId, CardStatus newStatus) {
        User operator = userAccessService.requireActiveUser(operatorId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
        card = cardLifecycleService.refreshExpiration(card);
        ensureCanChangeStatus(operator, card, newStatus);
        if (card.getStatus() == newStatus) {
            return cardMapper.toDto(card);
        }
        if (card.getStatus() == CardStatus.BLOCKED && newStatus == CardStatus.CLOSED) {
            throw new CardInactiveException("Blocked card cannot be directly closed");
        }
        if (card.getStatus() == CardStatus.EXPIRED && newStatus == CardStatus.ACTIVE) {
            throw new CardInactiveException("Expired card cannot be reactivated");
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

    private void ensureCanManageUser(User operator, Long targetUserId) {
        if (userAccessService.isAdmin(operator)) {
            return;
        }
        userAccessService.ensureUserRole(operator);
        if (!operator.getId().equals(targetUserId)) {
            throw new AccessDeniedException("User " + operator.getId() + " cannot manage cards of user " + targetUserId);
        }
    }

    private void ensureCanViewCard(User operator, Card card) {
        if (userAccessService.isAdmin(operator)) {
            return;
        }
        userAccessService.ensureUserRole(operator);
        if (card.getOwner() == null || !card.getOwner().getId().equals(operator.getId())) {
            throw new AccessDeniedException("User " + operator.getId() + " cannot view card " + card.getId());
        }
    }

    private void ensureCanChangeStatus(User operator, Card card, CardStatus newStatus) {
        if (userAccessService.isAdmin(operator)) {
            return;
        }
        userAccessService.ensureUserRole(operator);
        if (card.getOwner() == null || !card.getOwner().getId().equals(operator.getId())) {
            throw new AccessDeniedException("User " + operator.getId() + " cannot change status of card " + card.getId());
        }
        if (newStatus == CardStatus.CLOSED) {
            throw new AccessDeniedException("Card owner cannot close the card directly");
        }
        if (newStatus != CardStatus.ACTIVE && newStatus != CardStatus.BLOCKED) {
            throw new AccessDeniedException("Unsupported status change for non-admin user");
        }
    }
}