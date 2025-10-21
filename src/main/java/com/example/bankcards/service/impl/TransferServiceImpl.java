package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.BankcardsException;
import com.example.bankcards.exception.CardInactiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidTransferRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UserInactiveException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardTransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardLifecycleService;
import com.example.bankcards.service.NotificationService;
import com.example.bankcards.service.TransferLimitService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.CardNumberEncoder;
import com.example.bankcards.util.CardTransferMapper;
import com.example.bankcards.util.ValidationUtils;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final CardRepository cardRepository;
    private final CardTransferRepository cardTransferRepository;
    private final TransferLimitService transferLimitService;
    private final CardLifecycleService cardLifecycleService;
    private final NotificationService notificationService;
    private final CardNumberEncoder cardNumberEncoder;
    private final CardTransferMapper cardTransferMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(dontRollbackOn = BankcardsException.class)
    public CardTransferDto transfer(Long operatorId, CardTransferRequestDto request) {
        User operator = requireActiveUser(operatorId);
        String fromCardNumber = normalizeCardNumber(request.fromCardNumber());
        String toCardNumber = normalizeCardNumber(request.toCardNumber());
        if (fromCardNumber.equals(toCardNumber)) {
            throw new InvalidTransferRequestException("Source and target cards must be different");
        }
        BigDecimal amount;
        try {
            amount = ValidationUtils.normalizeAmount(request.amount());
        } catch (IllegalArgumentException ex) {
            throw new InvalidTransferRequestException(ex.getMessage());
        }

        Card fromCard = findCardByNumber(fromCardNumber);
        Card toCard = findCardByNumber(toCardNumber);

        fromCard = cardLifecycleService.refreshExpiration(fromCard);
        toCard = cardLifecycleService.refreshExpiration(toCard);

        ensureCanInitiateTransfer(operator, fromCard);

        CardTransfer transfer = new CardTransfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(amount);
        transfer.setDescription(request.description());

        try {
            validateOwnerIsActive(fromCard);
            validateOwnerIsActive(toCard);
            validateCardIsActive(fromCard);
            validateCardIsActive(toCard);

            transferLimitService.validateDailyLimit(fromCard.getId(), amount);

            if (fromCard.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Not enough funds to perform transfer");
            }

            fromCard.setBalance(fromCard.getBalance().subtract(amount));
            toCard.setBalance(toCard.getBalance().add(amount));

            transfer.setStatus(TransferStatus.COMPLETED);
            CardTransfer saved = cardTransferRepository.save(transfer);
            log.info("Completed transfer {} -> {} for amount {}", fromCard.getId(), toCard.getId(), amount);
            notificationService.notifyTransfer(saved);
            return cardTransferMapper.toDto(saved);
        } catch (BankcardsException ex) {
            transfer.setStatus(TransferStatus.FAILED);
            cardTransferRepository.save(transfer);
            log.warn("Transfer {} -> {} for amount {} failed: {}", fromCard.getId(), toCard.getId(), amount, ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<CardTransferDto> getTransfersForCard(Long operatorId, Long cardId) {
        User operator = requireActiveUser(operatorId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
        ensureCanViewCardTransfers(operator, card);
        cardLifecycleService.refreshExpiration(card);
        return Stream.concat(
                        cardTransferRepository.findAllByFromCardId(card.getId()).stream(),
                        cardTransferRepository.findAllByToCardId(card.getId()).stream()
                )
                .sorted(Comparator.comparing(CardTransfer::getCreatedAt).reversed())
                .map(cardTransferMapper::toDto)
                .collect(Collectors.toList());
    }

    private Card findCardByNumber(String normalizedCardNumber) {
        String encrypted = cardNumberEncoder.encrypt(normalizedCardNumber);
        return cardRepository.findByEncryptedNumber(encrypted)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found for number"));
    }

    private void validateCardIsActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardInactiveException("Card " + card.getId() + " is not active");
        }
    }

    private void validateOwnerIsActive(Card card) {
        if (card.getOwner() != null && card.getOwner().getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException("Owner of card " + card.getId() + " is not active");
        }
    }

    private String normalizeCardNumber(String cardNumber) {
        return cardNumber.replaceAll("\\s", "");
    }

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException("User " + userId + " is not active");
        }
        return user;
    }

    private void ensureCanInitiateTransfer(User operator, Card fromCard) {
        if (isAdmin(operator)) {
            return;
        }
        ensureUserRole(operator);
        if (fromCard.getOwner() == null || !fromCard.getOwner().getId().equals(operator.getId())) {
            throw new AccessDeniedException("User " + operator.getId() + " cannot initiate transfer from card " + fromCard.getId());
        }
    }

    private void ensureCanViewCardTransfers(User operator, Card card) {
        if (isAdmin(operator)) {
            return;
        }
        ensureUserRole(operator);
        if (card.getOwner() == null || !card.getOwner().getId().equals(operator.getId())) {
            throw new AccessDeniedException("User " + operator.getId() + " cannot view transfers for card " + card.getId());
        }
    }

    private boolean isAdmin(User user) {
        return hasRole(user.getRoles(), RoleType.ADMIN);
    }

    private void ensureUserRole(User user) {
        if (!hasRole(user.getRoles(), RoleType.USER)) {
            throw new AccessDeniedException("User " + user.getId() + " must have USER role for this operation");
        }
    }

    private boolean hasRole(Set<Role> roles, RoleType roleType) {
        return roles.stream().anyMatch(role -> role.getName() == roleType);
    }
}