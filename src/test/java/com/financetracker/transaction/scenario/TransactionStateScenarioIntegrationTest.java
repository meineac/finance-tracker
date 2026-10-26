package com.financetracker.transaction.scenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.account.api.web.CreateAccountRequest;
import com.financetracker.account.core.model.AccountType;
import com.financetracker.transaction.api.web.CreateCategoryRequest;
import com.financetracker.transaction.api.web.CreateTransactionRequest;
import com.financetracker.transaction.core.model.TransactionType;
import com.financetracker.user.api.web.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Transaction Entity Lifecycle & State Transitions Scenario Tests")
class TransactionStateScenarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Scenario 1: Full Successful Lifecycle Sequence ([*] -> Инициализация -> ПроверкаВладения -> ПроверкаКатегории -> ОбновлениеБаланса -> Завершена)")
    void fullLifecycleSuccessScenario() throws Exception {
        // --- 1. Arrange: Регистрация нового пользователя ---
        RegisterRequest registerReq = new RegisterRequest("scenario.user@example.com", "pass12345", "Scenario User");
        MvcResult regResult = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String token = "Bearer " + extractToken(regResult);

        // --- 2. Создание счета (CARD) ---
        CreateAccountRequest accountReq = new CreateAccountRequest("Primary Card", AccountType.CARD);
        MvcResult accResult = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(0))
                .andReturn();

        Long accountId = extractId(accResult);

        // --- 3. Создание категорий (Salary и Groceries) ---
        CreateCategoryRequest salaryCatReq = new CreateCategoryRequest("Salary Category");
        MvcResult salaryCatResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salaryCatReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long salaryCatId = extractId(salaryCatResult);

        CreateCategoryRequest groceriesCatReq = new CreateCategoryRequest("Groceries Category");
        MvcResult groceriesCatResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groceriesCatReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long groceriesCatId = extractId(groceriesCatResult);

        // --- 4. Act: Переход состояний для доходной транзакции (INCOME +5000.00) ---
        // Инициализация -> ПроверкаВладения -> ПроверкаКатегории -> ОбновлениеБаланса -> Завершена
        CreateTransactionRequest incomeTxReq = new CreateTransactionRequest(
                accountId, salaryCatId, TransactionType.INCOME, new BigDecimal("5000.00"), "Monthly Salary"
        );
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incomeTxReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.type").value("INCOME"));

        // --- 5. Assert: Проверка обновления баланса счёта (0.00 -> 5000.00) ---
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(5000.00));

        // --- 6. Act: Переход состояний для расходной транзакции (EXPENSE -1200.00) ---
        // Инициализация -> ПроверкаВладения -> ПроверкаКатегории -> ОбновлениеБаланса -> Завершена
        CreateTransactionRequest expenseTxReq = new CreateTransactionRequest(
                accountId, groceriesCatId, TransactionType.EXPENSE, new BigDecimal("1200.00"), "Supermarket Groceries"
        );
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseTxReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.amount").value(1200.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"));

        // --- 7. Assert: Проверка обновлённого баланса (5000.00 - 1200.00 = 3800.00) ---
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(3800.00));

        // --- 8. Assert: Проверка истории транзакций по счёту (сортировка по убыванию времени) ---
        mockMvc.perform(get("/api/transactions/account/" + accountId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Supermarket Groceries")) // более свежая
                .andExpect(jsonPath("$[1].description").value("Monthly Salary"));      // более старая
    }

    @Test
    @DisplayName("Scenario 2: State Transition 'Инициализация -> Отклонена' due to validation failure (amount <= 0)")
    void stateTransition_WhenInvalidAmount_ShouldRejectAtInitialization() throws Exception {
        // Arrange
        String token = registerAndGetToken("user.val.fail@example.com");
        Long accountId = createAccountAndGetId(token, "Card Val Fail");
        Long categoryId = createCategoryAndGetId(token, "Cat Val Fail");

        // Act: отправка невалидной транзакции (отрицательная сумма -50.00)
        CreateTransactionRequest invalidTx = new CreateTransactionRequest(
                accountId, categoryId, TransactionType.INCOME, new BigDecimal("-50.00"), "Negative"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTx)))
                .andExpect(status().isBadRequest());

        // Assert: баланс остался неизменным (0.00)
        mockMvc.perform(get("/api/accounts").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(0.00));
    }

    @Test
    @DisplayName("Scenario 3: State Transition 'ПроверкаВладения -> Отклонена' when account does not belong to user")
    void stateTransition_WhenAccountBelongsToOtherUser_ShouldRejectAtOwnershipCheck() throws Exception {
        // Arrange: Пользователь 1 создает счёт
        String token1 = registerAndGetToken("owner.user1@example.com");
        Long accountId1 = createAccountAndGetId(token1, "Owner 1 Card");

        // Пользователь 2 создает категорию
        String token2 = registerAndGetToken("intruder.user2@example.com");
        Long categoryId2 = createCategoryAndGetId(token2, "Category User 2");

        // Act: Пользователь 2 пытается создать транзакцию на счёте Пользователя 1
        CreateTransactionRequest maliciousTx = new CreateTransactionRequest(
                accountId1, categoryId2, TransactionType.INCOME, new BigDecimal("1000.00"), "Hijack"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousTx)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Account not found"));

        // Assert: Баланс счёта Пользователя 1 не изменился
        mockMvc.perform(get("/api/accounts").header("Authorization", token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(0.00));
    }

    @Test
    @DisplayName("Scenario 4: State Transition 'ПроверкаКатегории -> Отклонена' when category not found")
    void stateTransition_WhenCategoryNotFound_ShouldRejectAtCategoryCheck() throws Exception {
        // Arrange
        String token = registerAndGetToken("user.cat.fail@example.com");
        Long accountId = createAccountAndGetId(token, "Card Cat Fail");

        // Act: несуществующий ID категории 999999
        CreateTransactionRequest invalidCategoryTx = new CreateTransactionRequest(
                accountId, 999999L, TransactionType.EXPENSE, new BigDecimal("100.00"), "Unknown Cat"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryTx)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Category not found."));

        // Assert: Баланс счёта не изменился
        mockMvc.perform(get("/api/accounts").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(0.00));
    }

    // Helper methods
    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest req = new RegisterRequest(email, "password123", "Test User");
        MvcResult res = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return "Bearer " + extractToken(res);
    }

    private Long createAccountAndGetId(String token, String name) throws Exception {
        CreateAccountRequest req = new CreateAccountRequest(name, AccountType.CARD);
        MvcResult res = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(res);
    }

    private Long createCategoryAndGetId(String token, String name) throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest(name);
        MvcResult res = mockMvc.perform(post("/api/categories")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return extractId(res);
    }

    private String extractToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
