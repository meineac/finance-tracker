package com.financetracker.transaction.core.service;

import com.financetracker.transaction.core.model.TransactionCategory;

import java.util.List;
import java.util.Optional;

public interface TransactionCategoryRepository {
    Optional<TransactionCategory> findByIdAndUserId(Long id, Long userId);
    Optional<TransactionCategory> findByNameAndUserId(String name, Long userId);
    List<TransactionCategory> findAllVisibleToUser(Long userId);
    TransactionCategory save(TransactionCategory category);
}
