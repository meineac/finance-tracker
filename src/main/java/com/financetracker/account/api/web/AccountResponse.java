package com.financetracker.account.api.web;

import com.financetracker.account.core.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Account data returned to the client")
public record AccountResponse(
        Long id,
        String name,
        AccountType type,
        BigDecimal balance
) {}
