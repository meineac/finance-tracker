package com.financetracker.user.infrastructure.security;

import com.financetracker.shared.api.web.IdentifiableUserDetails;
import com.financetracker.user.core.model.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class SecurityUser implements IdentifiableUserDetails {
    private final User user;

    @Override
    public Long getId() {
        return user.id();
    }

    @Override
    @NonNull
    public String getUsername() {
        return user.email();
    }

    @Override
    public String getPassword() {
        return user.password();
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
}
