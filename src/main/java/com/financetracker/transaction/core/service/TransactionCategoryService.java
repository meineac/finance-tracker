package com.financetracker.transaction.core.service;

import com.financetracker.transaction.core.model.TransactionCategory;

import java.util.List;

public interface TransactionCategoryService {
    TransactionCategory createCategory(Long userId, String name);
    List<TransactionCategory> getUserCategories(Long userId);
}
