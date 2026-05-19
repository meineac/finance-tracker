package com.financetracker.account.core.service;

import com.financetracker.account.core.model.Account;
import com.financetracker.account.core.model.AccountType;
import com.financetracker.shared.core.exception.ResourceNotFoundException;
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

    @Override
    @Transactional(readOnly = true)
    public Account getByIdAndUserId(Long id, Long userId) {
        return accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Override
    @Transactional
    public void adjustBalance(Long accountId, BigDecimal amount) {
        accountRepository.adjustBalance(accountId, amount);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateOwnership(Long accountId, Long userId) {
        if (!accountRepository.existsByIdAndUserId(accountId, userId)) {
            throw new ResourceNotFoundException("Account not found");
        }
    }
}
