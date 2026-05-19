package com.financetracker.transaction.api.web;

import com.financetracker.shared.api.annotation.CurrentUserId;
import com.financetracker.transaction.core.model.Transaction;
import com.financetracker.transaction.core.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transactions", description = "Endpoints for managing user transactions")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new transaction")
    public TransactionResponse createTransaction(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = transactionService.createTransaction(
                userId,
                request.accountId(),
                request.categoryId(),
                request.type(),
                request.amount(),
                request.description()
        );

        return TransactionResponse.from(transaction);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get all transactions for a specific account")
    public List<TransactionResponse> getAccountTransactions(
            @CurrentUserId Long userId,
            @PathVariable Long accountId) {

        return transactionService.getAccountTransactions(userId, accountId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
