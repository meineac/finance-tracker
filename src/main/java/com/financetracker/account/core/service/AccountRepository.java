package com.financetracker.account.core.service;

import com.financetracker.account.core.model.Account;

import java.util.List;

public interface AccountRepository {
    List<Account> findAllByUserId(Long userId);
    Account save(Account account);
}
