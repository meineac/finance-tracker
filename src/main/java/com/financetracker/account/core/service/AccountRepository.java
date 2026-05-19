package com.financetracker.account.core.service;

import com.financetracker.account.core.model.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findAllByUserId(Long userId);
    Account save(Account account);
    Optional<Account> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    void adjustBalance(Long accountId, BigDecimal amount);
}
