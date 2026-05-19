package com.financetracker.transaction.core.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Transaction(
        Long id,
        Long accountId,
        Long categoryId,
        TransactionType type,
        BigDecimal amount,
        String description,
        OffsetDateTime timestamp
) {}
