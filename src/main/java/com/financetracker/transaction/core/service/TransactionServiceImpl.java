package com.financetracker.transaction.core.service;

import com.financetracker.account.core.service.AccountService;
import com.financetracker.shared.core.exception.ResourceNotFoundException;
import com.financetracker.transaction.core.model.Transaction;
import com.financetracker.transaction.core.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final AccountService accountService;

    @Override
    @Transactional
    public Transaction createTransaction(
            Long userId, Long accountId, Long categoryId,
            TransactionType type, BigDecimal amount, String description) {

        accountService.validateOwnership(accountId, userId);

        transactionCategoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        BigDecimal adjustment = (type == TransactionType.INCOME)
                ? amount
                : amount.negate();

        accountService.adjustBalance(accountId, adjustment);

        Transaction transaction = new Transaction(
                null,
                accountId,
                categoryId,
                type,
                amount,
                description,
                OffsetDateTime.now()
        );

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAccountTransactions(Long userId, Long accountId) {
        accountService.validateOwnership(accountId, userId);
        return transactionRepository.findAllByAccountId(accountId);
    }
}

