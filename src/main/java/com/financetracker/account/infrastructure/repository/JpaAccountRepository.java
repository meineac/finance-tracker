package com.financetracker.account.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaAccountRepository extends JpaRepository<AccountJpaEntity, Long> {
    List<AccountJpaEntity> findAllByUserId(Long userId);
}
