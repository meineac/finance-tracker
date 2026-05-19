package com.financetracker.transaction.api.web;

import com.financetracker.shared.api.annotation.CurrentUserId;
import com.financetracker.transaction.core.model.TransactionCategory;
import com.financetracker.transaction.core.service.TransactionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transaction Categories", description = "Endpoints for managing transaction categories")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class TransactionCategoryController {

    private final TransactionCategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new transaction category")
    public CategoryResponse createCategory(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateCategoryRequest request) {

        TransactionCategory category = categoryService.createCategory(userId, request.name());
        return CategoryResponse.from(category);
    }

    @GetMapping
    @Operation(summary = "Get all visible transaction categories (user specific and system default)")
    public List<CategoryResponse> getUserCategories(@CurrentUserId Long userId) {
        return categoryService.getUserCategories(userId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}