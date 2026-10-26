package com.financetracker.account.infrastructure.repository;

import com.financetracker.account.core.model.AccountType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("AccountRepository JPA Integration Tests")
class AccountRepositoryIntegrationTest {

    @Autowired
    private JpaAccountRepository jpaAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save & findAllByUserId: Should persist and fetch user accounts")
    void saveAndFindAllByUserId_ShouldPersistAndRetrieve() {
        // Arrange
        Long userId = 100L;
        AccountJpaEntity acc1 = new AccountJpaEntity();
        acc1.setUserId(userId);
        acc1.setName("Card 1");
        acc1.setType(AccountType.CARD);
        acc1.setBalance(new BigDecimal("500.00"));
        jpaAccountRepository.save(acc1);

        AccountJpaEntity acc2 = new AccountJpaEntity();
        acc2.setUserId(userId);
        acc2.setName("Cash 1");
        acc2.setType(AccountType.CASH);
        acc2.setBalance(new BigDecimal("100.00"));
        jpaAccountRepository.save(acc2);

        // Act
        List<AccountJpaEntity> accounts = jpaAccountRepository.findAllByUserId(userId);

        // Assert
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(AccountJpaEntity::getName).containsExactlyInAnyOrder("Card 1", "Cash 1");
    }

    @Test
    @DisplayName("existsByIdAndUserId: Should return true only for matching id and userId")
    void existsByIdAndUserId_ShouldVerifyOwnership() {
        // Arrange
        AccountJpaEntity acc = new AccountJpaEntity();
        acc.setUserId(10L);
        acc.setName("Ownership Test");
        acc.setType(AccountType.CARD);
        acc.setBalance(BigDecimal.ZERO);
        AccountJpaEntity saved = jpaAccountRepository.save(acc);

        // Act & Assert
        assertThat(jpaAccountRepository.existsByIdAndUserId(saved.getId(), 10L)).isTrue();
        assertThat(jpaAccountRepository.existsByIdAndUserId(saved.getId(), 999L)).isFalse();
    }

    @ParameterizedTest(name = "Initial 1000.00 + adjustment {0} -> expected {1}")
    @CsvSource({
            "500.00, 1500.00",
            "-300.00, 700.00",
            "0.55, 1000.55",
            "-1000.00, 0.00"
    })
    @DisplayName("adjustBalance: Atomic SQL UPDATE should correctly modify balance in DB")
    void adjustBalance_AtomicUpdate_ShouldCorrectlyUpdateBalance(BigDecimal adjustment, BigDecimal expectedBalance) {
        // Arrange
        AccountJpaEntity acc = new AccountJpaEntity();
        acc.setUserId(1L);
        acc.setName("Atomic Test");
        acc.setType(AccountType.CARD);
        acc.setBalance(new BigDecimal("1000.00"));
        AccountJpaEntity saved = jpaAccountRepository.saveAndFlush(acc);

        // Act
        jpaAccountRepository.adjustBalance(saved.getId(), adjustment);
        entityManager.clear(); // Clear persistence context to force reload from DB

        // Assert
        Optional<AccountJpaEntity> refreshed = jpaAccountRepository.findById(saved.getId());
        assertThat(refreshed).isPresent();
        assertThat(refreshed.get().getBalance()).isEqualByComparingTo(expectedBalance);
    }
}
