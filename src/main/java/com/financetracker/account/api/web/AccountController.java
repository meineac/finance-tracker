package com.financetracker.account.api.web;

import com.financetracker.account.core.model.Account;
import com.financetracker.account.core.service.AccountService;
import com.financetracker.shared.api.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Accounts", description = "Endpoints for managing user accounts")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountResponse> createAccount(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateAccountRequest request) {

        Account account = accountService.createAccount(userId, request.name(), request.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @GetMapping
    @Operation(summary = "Get all accounts for the authenticated user")
    public ResponseEntity<List<AccountResponse>> getUserAccounts(
            @CurrentUserId Long userId) {

        List<AccountResponse> accounts = accountService.getUserAccounts(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(accounts);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.id(),
                account.name(),
                account.type(),
                account.balance()
        );
    }
}
