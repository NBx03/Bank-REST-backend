package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardTransfer;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.exception.CardInactiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidTransferRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardTransferRepository;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TransferServiceImpl implements TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final CardRepository cardRepository;
    private final CardTransferRepository cardTransferRepository;
    private final TransferLimitService transferLimitService;
    private final CardLifecycleService cardLifecycleService;
    private final NotificationService notificationService;
    private final CardNumberEncoder cardNumberEncoder;
    private final CardTransferMapper cardTransferMapper;

    public TransferServiceImpl(CardRepository cardRepository,
                               CardTransferRepository cardTransferRepository,
                               TransferLimitService transferLimitService,
                               NotificationService notificationService,
                               CardLifecycleService cardLifecycleService,
                               CardNumberEncoder cardNumberEncoder,
                               CardTransferMapper cardTransferMapper) {
        this.cardRepository = cardRepository;
        this.cardTransferRepository = cardTransferRepository;
        this.transferLimitService = transferLimitService;
        this.notificationService = notificationService;
        this.cardLifecycleService = cardLifecycleService;
        this.cardNumberEncoder = cardNumberEncoder;
        this.cardTransferMapper = cardTransferMapper;
    }

    @Override
    public CardTransferDto transfer(CardTransferRequestDto request) {
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

        validateCardIsActive(fromCard);
        validateCardIsActive(toCard);

        transferLimitService.validateDailyLimit(fromCard.getId(), amount);

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Not enough funds to perform transfer");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        CardTransfer transfer = new CardTransfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(amount);
        transfer.setDescription(request.description());
        transfer.setStatus(TransferStatus.COMPLETED);

        CardTransfer saved = cardTransferRepository.save(transfer);
        log.info("Completed transfer {} -> {} for amount {}", fromCard.getId(), toCard.getId(), amount);
        notificationService.notifyTransfer(saved);
        return cardTransferMapper.toDto(saved);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<CardTransferDto> getTransfersForCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
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

    private String normalizeCardNumber(String cardNumber) {
        return cardNumber.replaceAll("\\s", "");
    }
}