package com.financetracker.transaction.infrastructure.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TransactionCategoryRepository JPA Integration Tests")
class TransactionCategoryRepositoryIntegrationTest {

    @Autowired
    private JpaTransactionCategoryRepository repository;

    @ParameterizedTest(name = "Search query ''{0}'' should match persisted ''Groceries''")
    @ValueSource(strings = {"groceries", "GROCERIES", "Groceries", "GrOcErIeS"})
    @DisplayName("findByNameIgnoreCaseAndUserId: Should find category regardless of letter case")
    void findByNameIgnoreCaseAndUserId_ShouldMatchCaseInsensitively(String searchName) {
        // Arrange
        Long userId = 77L;
        TransactionCategoryJpaEntity entity = new TransactionCategoryJpaEntity();
        entity.setName("Groceries");
        entity.setUserId(userId);
        repository.saveAndFlush(entity);

        // Act
        Optional<TransactionCategoryJpaEntity> found = repository.findByNameIgnoreCaseAndUserId(searchName, userId);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Groceries");
        assertThat(found.get().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("findAllVisibleToUser: Should return system categories (userId is null) and user's categories, but not other users' categories")
    void findAllVisibleToUser_ShouldReturnSystemAndUserCategoriesOnly() {
        // Arrange
        Long targetUser = 1L;
        Long otherUser = 2L;

        // System category (userId is null)
        TransactionCategoryJpaEntity sysCat = new TransactionCategoryJpaEntity();
        sysCat.setName("System Utility");
        sysCat.setUserId(null);
        repository.save(sysCat);

        // Target user category
        TransactionCategoryJpaEntity userCat = new TransactionCategoryJpaEntity();
        userCat.setName("My Custom Cat");
        userCat.setUserId(targetUser);
        repository.save(userCat);

        // Other user category
        TransactionCategoryJpaEntity otherCat = new TransactionCategoryJpaEntity();
        otherCat.setName("Other Secret Cat");
        otherCat.setUserId(otherUser);
        repository.save(otherCat);

        repository.flush();

        // Act
        List<TransactionCategoryJpaEntity> visible = repository.findAllVisibleToUser(targetUser);

        // Assert
        assertThat(visible).extracting(TransactionCategoryJpaEntity::getName)
                .contains("System Utility", "My Custom Cat")
                .doesNotContain("Other Secret Cat");
    }
}
