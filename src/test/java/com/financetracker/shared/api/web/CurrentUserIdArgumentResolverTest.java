package com.financetracker.shared.api.web;

import com.financetracker.shared.api.annotation.CurrentUserId;
import com.financetracker.shared.core.exception.UnauthorizedAccessException;
import com.financetracker.user.core.model.User;
import com.financetracker.user.infrastructure.security.SecurityUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CurrentUserIdArgumentResolver Unit Tests")
class CurrentUserIdArgumentResolverTest {

    private CurrentUserIdArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CurrentUserIdArgumentResolver();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Dummy method for reflection
    void sampleMethod(@CurrentUserId Long validId, Long unannotatedId, @CurrentUserId String wrongType) {}

    @Test
    @DisplayName("supportsParameter: Should return true for @CurrentUserId Long")
    void supportsParameter_ValidParameter_ShouldReturnTrue() throws NoSuchMethodException {
        // Arrange
        Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
        MethodParameter param = new MethodParameter(method, 0);

        // Act & Assert
        assertThat(resolver.supportsParameter(param)).isTrue();
    }

    @Test
    @DisplayName("supportsParameter: Should return false for unannotated Long")
    void supportsParameter_UnannotatedParameter_ShouldReturnFalse() throws NoSuchMethodException {
        // Arrange
        Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
        MethodParameter param = new MethodParameter(method, 1);

        // Act & Assert
        assertThat(resolver.supportsParameter(param)).isFalse();
    }

    @Test
    @DisplayName("supportsParameter: Should return false for @CurrentUserId non-Long type")
    void supportsParameter_WrongType_ShouldReturnFalse() throws NoSuchMethodException {
        // Arrange
        Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
        MethodParameter param = new MethodParameter(method, 2);

        // Act & Assert
        assertThat(resolver.supportsParameter(param)).isFalse();
    }

    @Test
    @DisplayName("resolveArgument: When authenticated with SecurityUser, should return userId")
    void resolveArgument_WhenAuthenticated_ShouldReturnUserId() throws Exception {
        // Arrange
        User domainUser = new User(42L, "user@test.com", "pass", "User");
        SecurityUser securityUser = new SecurityUser(domainUser);
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
        MethodParameter param = new MethodParameter(method, 0);

        // Act
        Object result = resolver.resolveArgument(param, null, null, null);

        // Assert
        assertThat(result).isEqualTo(42L);
    }

    @Test
    @DisplayName("resolveArgument: When authentication is null, should throw UnauthorizedAccessException")
    void resolveArgument_WhenNoAuth_ShouldThrowException() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();
        Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
        MethodParameter param = new MethodParameter(method, 0);

        // Act & Assert
        assertThatThrownBy(() -> resolver.resolveArgument(param, null, null, null))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("User not authenticated");
    }

    @Test
    @DisplayName("resolveArgument: When principal is anonymousUser string, should throw UnauthorizedAccessException")
    void resolveArgument_WhenAnonymousUser_ShouldThrowException() throws Exception {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
        MethodParameter param = new MethodParameter(method, 0);

        // Act & Assert
        assertThatThrownBy(() -> resolver.resolveArgument(param, null, null, null))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("User not authenticated");
    }

    @Test
    @DisplayName("resolveArgument: When principal is not IdentifiableUserDetails, should throw UnauthorizedAccessException")
    void resolveArgument_WhenNotIdentifiable_ShouldThrowException() throws Exception {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken("plainStringPrincipal", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
        MethodParameter param = new MethodParameter(method, 0);

        // Act & Assert
        assertThatThrownBy(() -> resolver.resolveArgument(param, null, null, null))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("Invalid user identity");
    }

    @Test
    @DisplayName("resolveArgument: When user id is null, should throw UnauthorizedAccessException")
    void resolveArgument_WhenUserIdIsNull_ShouldThrowException() throws Exception {
        // Arrange
        User userWithoutId = new User(null, "user@test.com", "pass", "User");
        SecurityUser securityUser = new SecurityUser(userWithoutId);
        var auth = new UsernamePasswordAuthenticationToken(securityUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Method method = getClass().getDeclaredMethod("sampleMethod", Long.class, Long.class, String.class);
        MethodParameter param = new MethodParameter(method, 0);

        // Act & Assert
        assertThatThrownBy(() -> resolver.resolveArgument(param, null, null, null))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("User identity is malformed");
    }
}
