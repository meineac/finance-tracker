package com.financetracker.transaction.core.service;

import com.financetracker.transaction.core.model.Transaction;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findAllByAccountId(Long accountId);
}
