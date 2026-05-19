package com.financetracker.transaction.api.web;

import com.financetracker.transaction.core.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull(message = "Account ID is required") Long accountId,
        @NotNull(message = "Category ID is required") Long categoryId,
        @NotNull(message = "Transaction type is required") TransactionType type,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount,
        String description
) {}
