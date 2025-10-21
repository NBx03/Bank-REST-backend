package com.example.bankcards.controller;

import com.example.bankcards.controller.support.OperatorContextResolver;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.UpdateCardRequestDto;
import com.example.bankcards.dto.UpdateCardStatusRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для работы с банковскими картами.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final OperatorContextResolver operatorContextResolver;

    /**
     * Выпускает новую карту для пользователя.
     */
    @PostMapping("/users/{userId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto issueCard(@PathVariable Long userId,
                             @Valid @RequestBody CreateCardRequestDto request,
                             HttpServletRequest httpRequest) {
        Long operatorId = operatorContextResolver.resolveOperatorId(httpRequest);
        return cardService.issueCard(operatorId, userId, request);
    }

    /**
     * Возвращает сведения о карте по идентификатору.
     */
    @GetMapping("/cards/{cardId}")
    public CardDto getCard(@PathVariable Long cardId, HttpServletRequest httpRequest) {
        Long operatorId = operatorContextResolver.resolveOperatorId(httpRequest);
        return cardService.getCard(operatorId, cardId);
    }

    /**
     * Возвращает список карт пользователя.
     */
    @GetMapping("/users/{userId}/cards")
    public Page<CardDto> getUserCards(@PathVariable Long userId,
                                      @RequestParam(value = "status", required = false) CardStatus status,
                                      @PageableDefault Pageable pageable,
                                      HttpServletRequest httpRequest) {
        Long operatorId = operatorContextResolver.resolveOperatorId(httpRequest);
        return cardService.getUserCards(operatorId, userId, status, pageable);
    }

    /**
     * Обновляет статус карты.
     */
    @PatchMapping("/cards/{cardId}/status")
    public CardDto changeStatus(@PathVariable Long cardId,
                                @Valid @RequestBody UpdateCardStatusRequestDto request,
                                HttpServletRequest httpRequest) {
        Long operatorId = operatorContextResolver.resolveOperatorId(httpRequest);
        return cardService.changeStatus(operatorId, cardId, request.status());
    }

    /**
     * Обновляет данные карты.
     */
    @PutMapping("/cards/{cardId}")
    public CardDto updateCard(@PathVariable Long cardId,
                              @Valid @RequestBody UpdateCardRequestDto request,
                              HttpServletRequest httpRequest) {
        Long operatorId = operatorContextResolver.resolveOperatorId(httpRequest);
        return cardService.updateCard(operatorId, cardId, request);
    }

    /**
     * Удаляет карту.
     */
    @DeleteMapping("/cards/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long cardId, HttpServletRequest httpRequest) {
        Long operatorId = operatorContextResolver.resolveOperatorId(httpRequest);
        cardService.deleteCard(operatorId, cardId);
    }
}