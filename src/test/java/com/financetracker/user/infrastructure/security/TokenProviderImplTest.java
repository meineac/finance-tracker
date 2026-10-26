package com.financetracker.user.infrastructure.security;

import com.financetracker.shared.infrastructure.security.JwtService;
import com.financetracker.user.core.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenProviderImpl Unit Tests")
class TokenProviderImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private TokenProviderImpl tokenProvider;

    @Test
    @DisplayName("generateToken: Should load UserDetails and delegate token generation to JwtService")
    void generateToken_ShouldDelegateToJwtService() {
        // Arrange
        User user = new User(1L, "alice@test.com", "pass", "Alice");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("alice@test.com")
                .password("pass")
                .authorities(java.util.Collections.emptyList())
                .build();

        when(userDetailsService.loadUserByUsername("alice@test.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt.token.here");

        // Act
        String token = tokenProvider.generateToken(user);

        // Assert
        assertThat(token).isEqualTo("jwt.token.here");
        verify(userDetailsService).loadUserByUsername("alice@test.com");
        verify(jwtService).generateToken(userDetails);
    }
}
