package com.financetracker.user.core.service;

import com.financetracker.shared.core.exception.DuplicateResourceException;
import com.financetracker.shared.core.exception.UnauthorizedAccessException;
import com.financetracker.user.core.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("register: When valid data provided, should hash password, save user, and return saved user")
    void register_WhenValidData_ShouldHashPasswordAndSaveUser() {
        // Arrange
        String email = "alice@example.com";
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedHash";
        String fullName = "Alice Doe";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        User savedUser = new User(1L, email, encodedPassword, fullName);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authService.register(email, rawPassword, fullName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.password()).isEqualTo(encodedPassword);
        assertThat(result.fullName()).isEqualTo(fullName);

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: When email already exists, should throw DuplicateResourceException")
    void register_WhenEmailAlreadyExists_ShouldThrowDuplicateResourceException() {
        // Arrange
        String email = "existing@example.com";
        String rawPassword = "password123";
        String fullName = "Existing User";

        User existingUser = new User(1L, email, "hash", fullName);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.register(email, rawPassword, fullName))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email is already registered");

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest(name = "login with email={0}")
    @CsvSource({
            "alice@example.com, secret123, jwt-token-alice",
            "bob@domain.org, myPassword99, jwt-token-bob"
    })
    @DisplayName("login: When credentials are valid, should return JWT token")
    void login_WhenValidCredentials_ShouldReturnJwtToken(String email, String password, String expectedToken) {
        // Arrange
        User user = new User(10L, email, "encodedHash", "User Name");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "encodedHash")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn(expectedToken);

        // Act
        String token = authService.login(email, password);

        // Assert
        assertThat(token).isEqualTo(expectedToken);

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, "encodedHash");
        verify(tokenProvider).generateToken(user);
    }

    @Test
    @DisplayName("login: When user not found by email, should throw UnauthorizedAccessException")
    void login_WhenUserNotFound_ShouldThrowUnauthorizedAccessException() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "somePassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("login: When password does not match, should throw UnauthorizedAccessException")
    void login_WhenPasswordDoesNotMatch_ShouldThrowUnauthorizedAccessException() {
        // Arrange
        String email = "alice@example.com";
        String wrongPassword = "wrongPassword";
        User user = new User(1L, email, "encodedHash", "Alice");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, "encodedHash")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(email, wrongPassword))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(wrongPassword, "encodedHash");
        verifyNoInteractions(tokenProvider);
    }
}
