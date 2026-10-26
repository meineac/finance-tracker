package com.financetracker.transaction.core.service;

import com.financetracker.shared.core.exception.DuplicateResourceException;
import com.financetracker.transaction.core.model.TransactionCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionCategoryServiceImpl Unit Tests")
class TransactionCategoryServiceImplTest {

    @Mock
    private TransactionCategoryRepository categoryRepository;

    @InjectMocks
    private TransactionCategoryServiceImpl categoryService;

    @Test
    @DisplayName("createCategory: When valid unique name provided, should save and return category")
    void createCategory_WhenValid_ShouldSaveAndReturn() {
        // Arrange
        Long userId = 1L;
        String name = "Groceries";
        when(categoryRepository.findByNameAndUserId(name, userId)).thenReturn(Optional.empty());

        TransactionCategory savedCategory = new TransactionCategory(5L, name, userId);
        when(categoryRepository.save(any(TransactionCategory.class))).thenReturn(savedCategory);

        // Act
        TransactionCategory result = categoryService.createCategory(userId, name);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo(name);
        assertThat(result.userId()).isEqualTo(userId);

        verify(categoryRepository).findByNameAndUserId(name, userId);
        ArgumentCaptor<TransactionCategory> captor = ArgumentCaptor.forClass(TransactionCategory.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo(name);
        assertThat(captor.getValue().userId()).isEqualTo(userId);
    }

    @ParameterizedTest(name = "Duplicate name variation: ''{0}''")
    @ValueSource(strings = {"Food", "food", "FOOD", "fOoD"})
    @DisplayName("createCategory: When duplicate category exists (case-insensitively), should throw DuplicateResourceException")
    void createCategory_WhenDuplicate_ShouldThrowDuplicateResourceException(String name) {
        // Arrange
        Long userId = 1L;
        TransactionCategory existing = new TransactionCategory(2L, name, userId);
        when(categoryRepository.findByNameAndUserId(name, userId)).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(userId, name))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Category with name '" + name + "' already exists");

        verify(categoryRepository).findByNameAndUserId(name, userId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserCategories: Should return all categories visible to user (user-created + system)")
    void getUserCategories_ShouldReturnVisibleCategories() {
        // Arrange
        Long userId = 1L;
        List<TransactionCategory> expected = List.of(
                new TransactionCategory(1L, "System Salary", null),
                new TransactionCategory(2L, "Custom Hobby", userId)
        );
        when(categoryRepository.findAllVisibleToUser(userId)).thenReturn(expected);

        // Act
        List<TransactionCategory> actual = categoryService.getUserCategories(userId);

        // Assert
        assertThat(actual).hasSize(2).isEqualTo(expected);
        verify(categoryRepository).findAllVisibleToUser(userId);
    }
}
