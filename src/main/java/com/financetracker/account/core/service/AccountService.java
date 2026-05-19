package com.financetracker.account.core.service;

import com.financetracker.account.core.model.Account;
import com.financetracker.account.core.model.AccountType;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    Account createAccount(Long userId, String name, AccountType type);
    List<Account> getUserAccounts(Long userId);
    Account getByIdAndUserId(Long id, Long userId);
    void adjustBalance(Long accountId, BigDecimal amount);
    void validateOwnership(Long accountId, Long userId);
}
