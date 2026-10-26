package com.financetracker.transaction.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.shared.api.advice.GlobalExceptionHandler;
import com.financetracker.shared.api.web.CurrentUserIdArgumentResolver;
import com.financetracker.transaction.core.model.TransactionCategory;
import com.financetracker.transaction.core.service.TransactionCategoryService;
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
@DisplayName("TransactionCategoryController Web Tests")
class TransactionCategoryControllerTest {

    @Mock
    private TransactionCategoryService categoryService;

    @InjectMocks
    private TransactionCategoryController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        // Authenticate user ID 5
        User domainUser = new User(5L, "cat@test.com", "pass", "Cat User");
        SecurityUser securityUser = new SecurityUser(domainUser);
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/categories: When valid name, should create category and return 201 Created")
    void createCategory_WhenValid_ShouldReturn201() throws Exception {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest("Utilities");
        TransactionCategory saved = new TransactionCategory(3L, "Utilities", 5L);
        when(categoryService.createCategory(eq(5L), eq("Utilities"))).thenReturn(saved);

        // Act & Assert
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Utilities"))
                .andExpect(jsonPath("$.isSystem").value(false));

        verify(categoryService).createCategory(5L, "Utilities");
    }

    @Test
    @DisplayName("POST /api/categories: When name is blank, should return 400 Bad Request")
    void createCategory_WhenNameBlank_ShouldReturn400() throws Exception {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest("   ");

        // Act & Assert
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/categories: Should return list of categories visible to user")
    void getUserCategories_ShouldReturn200() throws Exception {
        // Arrange
        List<TransactionCategory> categories = List.of(
                new TransactionCategory(1L, "System Food", null),
                new TransactionCategory(2L, "My Rent", 5L)
        );
        when(categoryService.getUserCategories(5L)).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("System Food"))
                .andExpect(jsonPath("$[0].isSystem").value(true))
                .andExpect(jsonPath("$[1].name").value("My Rent"))
                .andExpect(jsonPath("$[1].isSystem").value(false));

        verify(categoryService).getUserCategories(5L);
    }
}
