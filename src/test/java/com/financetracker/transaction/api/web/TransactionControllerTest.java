package com.financetracker.transaction.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.shared.api.advice.GlobalExceptionHandler;
import com.financetracker.shared.api.web.CurrentUserIdArgumentResolver;
import com.financetracker.transaction.core.model.Transaction;
import com.financetracker.transaction.core.model.TransactionType;
import com.financetracker.transaction.core.service.TransactionService;
import com.financetracker.user.core.model.User;
import com.financetracker.user.infrastructure.security.SecurityUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionController Web Tests")
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        // Authenticate as user ID 1
        User domainUser = new User(1L, "tx@test.com", "pass", "Tx User");
        SecurityUser securityUser = new SecurityUser(domainUser);
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/transactions: When input is valid, should create transaction and return 201 Created")
    void createTransaction_WhenValid_ShouldReturn201() throws Exception {
        // Arrange
        CreateTransactionRequest request = new CreateTransactionRequest(
                10L, 20L, TransactionType.INCOME, new BigDecimal("250.00"), "Bonus"
        );
        Transaction saved = new Transaction(
                1L, 10L, 20L, TransactionType.INCOME, new BigDecimal("250.00"), "Bonus", OffsetDateTime.now()
        );

        when(transactionService.createTransaction(
                eq(1L), eq(10L), eq(20L), eq(TransactionType.INCOME), eq(new BigDecimal("250.00")), eq("Bonus")
        )).thenReturn(saved);

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.categoryId").value(20))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.description").value("Bonus"));

        verify(transactionService).createTransaction(
                1L, 10L, 20L, TransactionType.INCOME, new BigDecimal("250.00"), "Bonus"
        );
    }

    @ParameterizedTest(name = "Invalid amount JSON payload: {0}")
    @ValueSource(strings = {
            "{\"accountId\": 1, \"categoryId\": 1, \"type\": \"INCOME\", \"amount\": 0, \"description\": \"Zero\"}",
            "{\"accountId\": 1, \"categoryId\": 1, \"type\": \"EXPENSE\", \"amount\": -10.50, \"description\": \"Negative\"}",
            "{\"accountId\": null, \"categoryId\": 1, \"type\": \"INCOME\", \"amount\": 100, \"description\": \"Null account\"}",
            "{\"accountId\": 1, \"categoryId\": null, \"type\": \"INCOME\", \"amount\": 100, \"description\": \"Null category\"}",
            "{\"accountId\": 1, \"categoryId\": 1, \"type\": null, \"amount\": 100, \"description\": \"Null type\"}"
    })
    @DisplayName("POST /api/transactions: When validation fails, should return 400 Bad Request")
    void createTransaction_WhenValidationFails_ShouldReturn400(String jsonPayload) throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/transactions/account/{accountId}: Should return transaction list with 200 OK")
    void getAccountTransactions_ShouldReturn200() throws Exception {
        // Arrange
        Long accountId = 10L;
        List<Transaction> transactions = List.of(
                new Transaction(1L, accountId, 2L, TransactionType.INCOME, new BigDecimal("100.00"), "Initial", OffsetDateTime.now())
        );
        when(transactionService.getAccountTransactions(1L, accountId)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/account/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(100.00));

        verify(transactionService).getAccountTransactions(1L, accountId);
    }
}
