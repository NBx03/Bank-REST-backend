package com.example.bankcards.controller;

import static com.example.bankcards.testutil.TestDataFactory.cardDto;
import static com.example.bankcards.testutil.TestDataFactory.cardList;
import static com.example.bankcards.testutil.TestDataFactory.createCardRequest;
import static com.example.bankcards.testutil.TestDataFactory.updateCardStatusRequest;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bankcards.controller.support.OperatorContextResolver;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.dto.UpdateCardStatusRequestDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(OperatorContextResolver.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @Test
    void issueCard_shouldReturnCreatedCard() throws Exception {
        CreateCardRequestDto request = createCardRequest();
        CardDto response = cardDto(1L);
        when(cardService.issueCard(eq(10L), eq(5L), any(CreateCardRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/5/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(OperatorContextResolver.OPERATOR_HEADER, "10")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(1)));
    }

    @Test
    void issueCard_shouldReturnBadRequestWhenHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/users/5/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("X-Operator-Id header is required")));
    }

    @Test
    void getCard_shouldReturnCard() throws Exception {
        when(cardService.getCard(eq(10L), eq(1L))).thenReturn(cardDto(1L));

        mockMvc.perform(get("/api/cards/1")
                        .header(OperatorContextResolver.OPERATOR_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastDigits", equalTo("4444")));
    }

    @Test
    void getUserCards_shouldReturnList() throws Exception {
        when(cardService.getUserCards(eq(10L), eq(5L))).thenReturn(cardList());

        mockMvc.perform(get("/api/users/5/cards")
                        .header(OperatorContextResolver.OPERATOR_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void changeStatus_shouldReturnForbiddenWhenAccessDenied() throws Exception {
        UpdateCardStatusRequestDto request = updateCardStatusRequest(CardStatus.BLOCKED);
        when(cardService.changeStatus(eq(10L), eq(1L), eq(CardStatus.BLOCKED)))
                .thenThrow(new AccessDeniedException("Access denied"));

        mockMvc.perform(patch("/api/cards/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(OperatorContextResolver.OPERATOR_HEADER, "10")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", equalTo("Access denied")));
    }
}