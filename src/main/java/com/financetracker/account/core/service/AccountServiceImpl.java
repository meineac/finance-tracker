package com.financetracker.account.core.service;

import com.financetracker.account.core.model.Account;
import com.financetracker.account.core.model.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Account createAccount(Long userId, String name, AccountType type) {
        Account newAccount = new Account(null, userId, name, type, BigDecimal.ZERO);
        return accountRepository.save(newAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findAllByUserId(userId);
    }
}
