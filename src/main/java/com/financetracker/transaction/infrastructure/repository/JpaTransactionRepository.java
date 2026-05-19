package com.financetracker.transaction.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTransactionRepository extends JpaRepository<TransactionJpaEntity, Long> {
    List<TransactionJpaEntity> findByAccountIdOrderByTimestampDesc(Long accountId);
}
