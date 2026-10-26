package com.financetracker.transaction.infrastructure.repository;

import com.financetracker.transaction.core.model.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TransactionRepository JPA Integration Tests")
class TransactionRepositoryIntegrationTest {

    @Autowired
    private JpaTransactionRepository repository;

    @Test
    @DisplayName("findByAccountIdOrderByTimestampDesc: Should return transactions sorted descending by timestamp")
    void findByAccountIdOrderByTimestampDesc_ShouldReturnSortedDescending() {
        // Arrange
        Long accountId = 15L;
        Long otherAccountId = 16L;
        OffsetDateTime now = OffsetDateTime.now();

        TransactionJpaEntity tx1 = createTx(accountId, 1L, TransactionType.INCOME, new BigDecimal("100.00"), "First (oldest)", now.minusHours(2));
        TransactionJpaEntity tx2 = createTx(accountId, 2L, TransactionType.EXPENSE, new BigDecimal("50.00"), "Second (middle)", now.minusHours(1));
        TransactionJpaEntity tx3 = createTx(accountId, 1L, TransactionType.INCOME, new BigDecimal("200.00"), "Third (newest)", now);
        TransactionJpaEntity txOther = createTx(otherAccountId, 1L, TransactionType.INCOME, new BigDecimal("999.00"), "Other Account", now);

        repository.saveAllAndFlush(List.of(tx1, tx2, tx3, txOther));

        // Act
        List<TransactionJpaEntity> result = repository.findByAccountIdOrderByTimestampDesc(accountId);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDescription()).isEqualTo("Third (newest)");
        assertThat(result.get(1).getDescription()).isEqualTo("Second (middle)");
        assertThat(result.get(2).getDescription()).isEqualTo("First (oldest)");
        assertThat(result).extracting(TransactionJpaEntity::getAccountId).containsOnly(accountId);
    }

    private TransactionJpaEntity createTx(Long accountId, Long categoryId, TransactionType type,
                                         BigDecimal amount, String description, OffsetDateTime timestamp) {
        TransactionJpaEntity tx = new TransactionJpaEntity();
        tx.setAccountId(accountId);
        tx.setCategoryId(categoryId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setTimestamp(timestamp);
        return tx;
    }
}
