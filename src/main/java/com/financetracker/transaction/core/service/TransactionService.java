package com.financetracker.transaction.core.service;

import com.financetracker.transaction.core.model.Transaction;
import com.financetracker.transaction.core.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {
    Transaction createTransaction(
            Long userId,
            Long accountId,
            Long categoryId,
            TransactionType type,
            BigDecimal amount,
            String description
    );

    List<Transaction> getAccountTransactions(Long userId, Long accountId);
}
