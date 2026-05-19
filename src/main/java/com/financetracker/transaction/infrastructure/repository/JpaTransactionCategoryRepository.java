package com.financetracker.transaction.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaTransactionCategoryRepository extends JpaRepository<TransactionCategoryJpaEntity, Long> {
    Optional<TransactionCategoryJpaEntity> findByIdAndUserId(Long id, Long userId);
    Optional<TransactionCategoryJpaEntity> findByNameIgnoreCaseAndUserId(String name, Long userId);

    @Query("SELECT c FROM TransactionCategoryJpaEntity c WHERE c.userId = :userId OR c.userId IS NULL ORDER BY c.name")
    List<TransactionCategoryJpaEntity> findAllVisibleToUser(@Param("userId") Long userId);
}
