package com.financetracker.transaction.core.service;

import com.financetracker.shared.core.exception.DuplicateResourceException;
import com.financetracker.transaction.core.model.TransactionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionCategoryServiceImpl implements TransactionCategoryService {

    private final TransactionCategoryRepository categoryRepository;

    @Override
    @Transactional
    public TransactionCategory createCategory(Long userId, String name) {
        categoryRepository.findByNameAndUserId(name, userId)
                .ifPresent(_ -> {
                    throw new DuplicateResourceException("Category with name '" + name + "' already exists");
                });

        TransactionCategory newCategory = new TransactionCategory(null, name, userId);
        return categoryRepository.save(newCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionCategory> getUserCategories(Long userId) {
        return categoryRepository.findAllVisibleToUser(userId);
    }
}
