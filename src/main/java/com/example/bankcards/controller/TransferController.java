package com.example.bankcards.controller;

import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.dto.DailyLimitDto;
import com.example.bankcards.controller.support.OperatorContextResolver;
import com.example.bankcards.service.TransferLimitService;
import com.example.bankcards.service.TransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления переводами.
 */
@RestController
@RequestMapping("/api")
public class TransferController {

    private final TransferService transferService;
    private final TransferLimitService transferLimitService;
    private final OperatorContextResolver operatorContextResolver;

    public TransferController(TransferService transferService,
                              TransferLimitService transferLimitService,
                              OperatorContextResolver operatorContextResolver) {
        this.transferService = transferService;
        this.transferLimitService = transferLimitService;
        this.operatorContextResolver = operatorContextResolver;
    }

    /**
     * Выполняет перевод между картами.
     */
    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public CardTransferDto transfer(@Valid @RequestBody CardTransferRequestDto request,
                                    HttpServletRequest httpRequest) {
        Long operatorId = operatorContextResolver.resolveOperatorId(httpRequest);
        return transferService.transfer(operatorId, request);
    }

    /**
     * Возвращает историю переводов карты.
     */
    @GetMapping("/cards/{cardId}/transfers")
    public List<CardTransferDto> getTransfers(@PathVariable Long cardId, HttpServletRequest httpRequest) {
        Long operatorId = operatorContextResolver.resolveOperatorId(httpRequest);
        return transferService.getTransfersForCard(operatorId, cardId);
    }

    /**
     * Возвращает остаток суточного лимита карты.
     */
    @GetMapping("/cards/{cardId}/limits/daily")
    public DailyLimitDto getDailyLimit(@PathVariable Long cardId, HttpServletRequest httpRequest) {
        operatorContextResolver.resolveOperatorId(httpRequest);
        BigDecimal remaining = transferLimitService.getRemainingDailyLimit(cardId);
        return new DailyLimitDto(remaining);
    }
}