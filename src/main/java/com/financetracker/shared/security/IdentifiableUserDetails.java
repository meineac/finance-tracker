package com.financetracker.shared.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface IdentifiableUserDetails extends UserDetails {
    Long getId();
}
