package com.financetracker.transaction.infrastructure.repository;

import com.financetracker.transaction.core.model.Transaction;
import com.financetracker.transaction.core.service.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {
    private final JpaTransactionRepository jpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = toJpaEntity(transaction);
        TransactionJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }



    @Override
    public List<Transaction> findAllByAccountId(Long accountId) {
        return jpaRepository.findByAccountIdOrderByTimestampDesc(accountId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getAccountId(),
                entity.getCategoryId(),
                entity.getType(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getTimestamp()
        );
    }

    private TransactionJpaEntity toJpaEntity(Transaction transaction) {
        TransactionJpaEntity entity = new TransactionJpaEntity();
        entity.setId(transaction.id());
        entity.setAccountId(transaction.accountId());
        entity.setCategoryId(transaction.categoryId());
        entity.setType(transaction.type());
        entity.setAmount(transaction.amount());
        entity.setDescription(transaction.description());
        entity.setTimestamp(transaction.timestamp());
        return entity;
    }
}
