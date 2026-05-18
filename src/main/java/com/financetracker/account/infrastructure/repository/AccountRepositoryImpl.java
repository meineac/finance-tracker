package com.financetracker.account.infrastructure.repository;

import com.financetracker.account.core.model.*;

import com.financetracker.account.core.service.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {
    private final JpaAccountRepository jpaRepository;

    @Override
    public List<Account> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList()); // toList() ?
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = toJpaEntity(account);
        AccountJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    private Account toDomain(AccountJpaEntity entity) {
        return new Account(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getType(),
                entity.getBalance()
        );
    }

    private AccountJpaEntity toJpaEntity(Account domain) {
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setId(domain.id());
        entity.setUserId(domain.userId());
        entity.setName(domain.name());
        entity.setType(domain.type());
        entity.setBalance(domain.balance());
        return entity;
    }
}
