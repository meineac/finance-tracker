package com.financetracker.shared.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "very_long_secure_secret_key_for_testing_purposes_only_at_least_256_bits!";
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("generateToken: Should generate valid JWT containing username as subject")
    void generateToken_ShouldContainSubject() {
        // Arrange
        UserDetails userDetails = new User("user@example.com", "pass", Collections.emptyList());

        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("isTokenValid: Should return true when token matches userDetails and is not expired")
    void isTokenValid_WhenValid_ShouldReturnTrue() {
        // Arrange
        UserDetails userDetails = new User("alice@example.com", "pass", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: Should return false when username in token does not match userDetails")
    void isTokenValid_WhenUsernameMismatch_ShouldReturnFalse() {
        // Arrange
        UserDetails userDetails1 = new User("user1@example.com", "pass", Collections.emptyList());
        UserDetails userDetails2 = new User("user2@example.com", "pass", Collections.emptyList());
        String token = jwtService.generateToken(userDetails1);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails2);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("extractUsername: When token is expired, should throw exception")
    void extractUsername_WhenExpired_ShouldThrowException() {
        // Arrange: expired token with negative expiration
        JwtService expiredJwtService = new JwtService();
        ReflectionTestUtils.setField(expiredJwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(expiredJwtService, "jwtExpiration", -1000L);

        UserDetails userDetails = new User("expired@example.com", "pass", Collections.emptyList());
        String token = expiredJwtService.generateToken(userDetails);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}
