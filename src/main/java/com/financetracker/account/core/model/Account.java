package com.financetracker.account.core.model;

import java.math.BigDecimal;

public record Account(
        Long id,
        Long userId,
        String name,
        AccountType type,
        BigDecimal balance
) {}
