package com.financetracker.user.api.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication token response payload")
public record AuthResponse(
        String token
) {}
