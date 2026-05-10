package com.financetracker.shared.model;

import org.springframework.security.core.userdetails.UserDetails;

public interface IdentifiableUserDetails extends UserDetails {
    Long getId();
}
