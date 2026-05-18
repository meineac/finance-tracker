package com.financetracker.user.core.model;

public record User(
        Long id,
        String email,
        String password,
        String fullName
) {}
