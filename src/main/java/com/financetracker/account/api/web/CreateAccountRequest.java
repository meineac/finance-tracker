package com.financetracker.account.api.web;

import com.financetracker.account.core.model.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for creating a new user account")
public record CreateAccountRequest(
        @Schema(example = "Main Checking", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Account name is required")
        String name,

        @Schema(example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Account type is required")
        AccountType type
) {}
