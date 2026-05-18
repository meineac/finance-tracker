package com.financetracker.user.infrastructure.security;

import com.financetracker.shared.infrastructure.security.JwtService;
import com.financetracker.user.core.model.User;
import com.financetracker.user.core.service.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenProviderImpl implements TokenProvider {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public String generateToken(User user) {
        var userDetails = userDetailsService.loadUserByUsername(user.email());
        return jwtService.generateToken(userDetails);
    }
}
