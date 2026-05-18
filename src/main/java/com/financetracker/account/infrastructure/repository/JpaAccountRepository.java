package com.financetracker.account.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaAccountRepository extends JpaRepository<AccountJpaEntity, Long> {
    List<AccountJpaEntity> findAllByUserId(Long userId);
    Optional<AccountJpaEntity> findByIdAndUserId(Long id, Long userId);
}
