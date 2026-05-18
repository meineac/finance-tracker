package com.financetracker.account.core.service;

import com.financetracker.account.core.model.Account;
import com.financetracker.account.core.model.AccountType;

import java.util.List;

public interface AccountService {
    Account createAccount(Long userId, String name, AccountType type);
    List<Account> getUserAccounts(Long userId);
    Account getByIdAndUserId(Long id, Long userId);
}
