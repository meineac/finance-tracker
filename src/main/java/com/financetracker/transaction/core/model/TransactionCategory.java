package com.financetracker.transaction.core.model;

public record TransactionCategory(
        Long id,
        String name,
        Long userId
) {}
