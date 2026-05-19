package com.financetracker.transaction.api.web;

import com.financetracker.transaction.core.model.Transaction;
import com.financetracker.transaction.core.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionResponse(
        Long id,
        Long accountId,
        Long categoryId,
        TransactionType type,
        BigDecimal amount,
        String description,
        OffsetDateTime timestamp
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.accountId(),
                transaction.categoryId(),
                transaction.type(),
                transaction.amount(),
                transaction.description(),
                transaction.timestamp()
        );
    }
}
