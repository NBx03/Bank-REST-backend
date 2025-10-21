package com.example.bankcards.controller;

import static com.example.bankcards.testutil.TestDataFactory.cardTransferDto;
import static com.example.bankcards.testutil.TestDataFactory.dailyLimit;
import static com.example.bankcards.testutil.TestDataFactory.transferList;
import static com.example.bankcards.testutil.TestDataFactory.transferRequest;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.controller.support.OperatorContextResolver;
import com.example.bankcards.dto.CardTransferDto;
import com.example.bankcards.dto.CardTransferRequestDto;
import com.example.bankcards.dto.DailyLimitDto;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.service.TransferLimitService;
import com.example.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(OperatorContextResolver.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @MockBean
    private TransferLimitService transferLimitService;

    @Test
    void transfer_shouldReturnCreatedTransfer() throws Exception {
        CardTransferRequestDto request = transferRequest();
        CardTransferDto response = cardTransferDto(1L);
        when(transferService.transfer(eq(10L), any(CardTransferRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(OperatorContextResolver.OPERATOR_HEADER, "10")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(1)));
    }

    @Test
    void transfer_shouldReturnBusinessErrorWhenInsufficientFunds() throws Exception {
        when(transferService.transfer(eq(10L), any(CardTransferRequestDto.class)))
                .thenThrow(new InsufficientFundsException("Not enough funds"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(OperatorContextResolver.OPERATOR_HEADER, "10")
                        .content(objectMapper.writeValueAsString(transferRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", equalTo("Not enough funds")));
    }

    @Test
    void getTransfers_shouldReturnList() throws Exception {
        when(transferService.getTransfersForCard(eq(10L), eq(5L))).thenReturn(transferList());

        mockMvc.perform(get("/api/cards/5/transfers")
                        .header(OperatorContextResolver.OPERATOR_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getDailyLimit_shouldReturnRemainingLimit() throws Exception {
        DailyLimitDto response = dailyLimit(BigDecimal.valueOf(2500));
        when(transferLimitService.getRemainingDailyLimit(5L)).thenReturn(response.remaining());

        mockMvc.perform(get("/api/cards/5/limits/daily")
                        .header(OperatorContextResolver.OPERATOR_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining", equalTo(2500)));
    }

    @Test
    void getDailyLimit_shouldReturnBadRequestWhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/cards/5/limits/daily"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("X-Operator-Id header is required")));
    }
}