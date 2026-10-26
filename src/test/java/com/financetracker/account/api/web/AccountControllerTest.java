package com.financetracker.account.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.account.core.model.Account;
import com.financetracker.account.core.model.AccountType;
import com.financetracker.account.core.service.AccountService;
import com.financetracker.shared.api.advice.GlobalExceptionHandler;
import com.financetracker.shared.api.web.CurrentUserIdArgumentResolver;
import com.financetracker.user.core.model.User;
import com.financetracker.user.infrastructure.security.SecurityUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController Web Tests")
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        // Authenticate as user ID 10
        User domainUser = new User(10L, "test@test.com", "pass", "Test User");
        SecurityUser securityUser = new SecurityUser(domainUser);
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/accounts: When input is valid, should create account and return 201 Created")
    void createAccount_WhenValid_ShouldReturn201() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("Tinkoff Black", AccountType.CARD);
        Account saved = new Account(1L, 10L, "Tinkoff Black", AccountType.CARD, BigDecimal.ZERO);
        when(accountService.createAccount(eq(10L), eq("Tinkoff Black"), eq(AccountType.CARD))).thenReturn(saved);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tinkoff Black"))
                .andExpect(jsonPath("$.type").value("CARD"))
                .andExpect(jsonPath("$.balance").value(0));

        verify(accountService).createAccount(10L, "Tinkoff Black", AccountType.CARD);
    }

    @Test
    @DisplayName("POST /api/accounts: When name is blank, should return 400 Bad Request")
    void createAccount_WhenNameBlank_ShouldReturn400() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("", AccountType.CARD);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/accounts: When type is null, should return 400 Bad Request")
    void createAccount_WhenTypeNull_ShouldReturn400() throws Exception {
        // Arrange
        String payload = "{\"name\": \"Card without type\", \"type\": null}";

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/accounts: Should return list of user accounts with 200 OK")
    void getUserAccounts_ShouldReturn200() throws Exception {
        // Arrange
        List<Account> accounts = List.of(
                new Account(1L, 10L, "Card", AccountType.CARD, new BigDecimal("100.00")),
                new Account(2L, 10L, "Cash", AccountType.CASH, new BigDecimal("50.00"))
        );
        when(accountService.getUserAccounts(10L)).thenReturn(accounts);

        // Act & Assert
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Card"))
                .andExpect(jsonPath("$[1].name").value("Cash"));

        verify(accountService).getUserAccounts(10L);
    }
}
