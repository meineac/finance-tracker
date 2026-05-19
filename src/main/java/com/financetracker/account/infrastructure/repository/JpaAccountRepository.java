package com.financetracker.account.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface JpaAccountRepository extends JpaRepository<AccountJpaEntity, Long> {
    List<AccountJpaEntity> findAllByUserId(Long userId);
    Optional<AccountJpaEntity> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE AccountJpaEntity a SET a.balance = a.balance + :amount WHERE a.id = :accountId")
    void adjustBalance(Long accountId, BigDecimal amount);
}
