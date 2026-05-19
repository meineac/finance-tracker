package com.financetracker.transaction.api.web;

import com.financetracker.transaction.core.model.TransactionCategory;

public record CategoryResponse(
        Long id,
        String name,
        boolean isSystem
) {
    public static CategoryResponse from(TransactionCategory category) {
        return new CategoryResponse(
                category.id(),
                category.name(),
                category.userId() == null
        );
    }
}