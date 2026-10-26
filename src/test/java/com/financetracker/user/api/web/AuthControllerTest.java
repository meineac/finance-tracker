package com.financetracker.user.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financetracker.shared.api.advice.GlobalExceptionHandler;
import com.financetracker.shared.core.exception.UnauthorizedAccessException;
import com.financetracker.user.core.model.User;
import com.financetracker.user.core.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Web Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/users/register: When input is valid, should register and return 201 Created with JWT")
    void register_WhenValid_ShouldReturn201() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("john@test.com", "secret123", "John Doe");
        User savedUser = new User(1L, "john@test.com", "hash", "John Doe");

        when(authService.register(eq("john@test.com"), eq("secret123"), eq("John Doe")))
                .thenReturn(savedUser);
        when(authService.login("john@test.com", "secret123")).thenReturn("mocked.jwt.token");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));

        verify(authService).register("john@test.com", "secret123", "John Doe");
        verify(authService).login("john@test.com", "secret123");
    }

    @ParameterizedTest(name = "Invalid registration payload: email={0}, password={1}, name={2}")
    @CsvSource({
            "'not-an-email', 'password123', 'John Doe'",
            "'', 'password123', 'John Doe'",
            "'john@test.com', '12345', 'John Doe'",
            "'john@test.com', 'password123', ''"
    })
    @DisplayName("POST /api/users/register: When input is invalid, should return 400 Bad Request")
    void register_WhenInvalidInput_ShouldReturn400(String email, String password, String fullName) throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest(email, password, fullName);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/login: When credentials are valid, should return 200 OK with JWT")
    void login_WhenValid_ShouldReturn200() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("john@test.com", "secret123");
        when(authService.login("john@test.com", "secret123")).thenReturn("token.abc.123");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token.abc.123"));

        verify(authService).login("john@test.com", "secret123");
    }

    @Test
    @DisplayName("POST /api/users/login: When credentials are invalid, should return 403 Forbidden")
    void login_WhenInvalidCredentials_ShouldReturn403() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("john@test.com", "wrongPassword");
        when(authService.login(any(), any()))
                .thenThrow(new UnauthorizedAccessException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Invalid credentials"));
    }
}
