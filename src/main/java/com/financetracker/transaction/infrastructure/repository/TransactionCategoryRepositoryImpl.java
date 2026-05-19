package com.financetracker.transaction.infrastructure.repository;

import com.financetracker.transaction.core.model.TransactionCategory;
import com.financetracker.transaction.core.service.TransactionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionCategoryRepositoryImpl implements TransactionCategoryRepository {
    private final JpaTransactionCategoryRepository  jpaRepository;
    @Override
    public Optional<TransactionCategory> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUserId(id, userId)
                .map(this::toDomain);

    }

    @Override
    public Optional<TransactionCategory> findByNameAndUserId(String name, Long userId) {
        return jpaRepository.findByNameIgnoreCaseAndUserId(name, userId)
                .map(this::toDomain);
    }

    @Override
    public List<TransactionCategory> findAllVisibleToUser(Long userId) {
        return jpaRepository.findAllVisibleToUser(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public TransactionCategory save(TransactionCategory category) {
        TransactionCategoryJpaEntity entity = toJpaEntity(category);
        TransactionCategoryJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    private TransactionCategory toDomain(TransactionCategoryJpaEntity entity) {
        return new TransactionCategory(
                entity.getId(),
                entity.getName(),
                entity.getUserId()
        );
    }

    private TransactionCategoryJpaEntity toJpaEntity(TransactionCategory domain) {
        TransactionCategoryJpaEntity entity = new TransactionCategoryJpaEntity();
        entity.setId(domain.id());
        entity.setName(domain.name());
        entity.setUserId(domain.userId());
        return entity;
    }
}
