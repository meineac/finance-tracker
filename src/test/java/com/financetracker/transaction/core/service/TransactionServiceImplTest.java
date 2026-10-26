package com.financetracker.transaction.core.service;

import com.financetracker.account.core.service.AccountService;
import com.financetracker.shared.core.exception.ResourceNotFoundException;
import com.financetracker.transaction.core.model.Transaction;
import com.financetracker.transaction.core.model.TransactionCategory;
import com.financetracker.transaction.core.model.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Unit Tests")
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @ParameterizedTest(name = "Type={0}, amount={1} -> expected adjustment={2}")
    @CsvSource({
            "INCOME, 500.00, 500.00",
            "EXPENSE, 150.00, -150.00",
            "INCOME, 0.01, 0.01",
            "EXPENSE, 999999.99, -999999.99"
    })
    @DisplayName("createTransaction: Should calculate correct adjustment and adjust balance")
    void createTransaction_ParameterizedTypes_ShouldAdjustBalanceAndSave(
            TransactionType type, BigDecimal amount, BigDecimal expectedAdjustment) {
        // Arrange
        Long userId = 1L;
        Long accountId = 10L;
        Long categoryId = 100L;
        String description = "Test transaction";

        doNothing().when(accountService).validateOwnership(accountId, userId);
        TransactionCategory category = new TransactionCategory(categoryId, "Category 1", userId);
        when(transactionCategoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));

        Transaction savedTx = new Transaction(1L, accountId, categoryId, type, amount, description, OffsetDateTime.now());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        // Act
        Transaction result = transactionService.createTransaction(userId, accountId, categoryId, type, amount, description);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.type()).isEqualTo(type);
        assertThat(result.amount()).isEqualByComparingTo(amount);

        verify(accountService).validateOwnership(accountId, userId);
        verify(transactionCategoryRepository).findByIdAndUserId(categoryId, userId);
        verify(accountService).adjustBalance(accountId, expectedAdjustment);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction captured = captor.getValue();
        assertThat(captured.accountId()).isEqualTo(accountId);
        assertThat(captured.categoryId()).isEqualTo(categoryId);
        assertThat(captured.type()).isEqualTo(type);
        assertThat(captured.amount()).isEqualByComparingTo(amount);
        assertThat(captured.description()).isEqualTo(description);
        assertThat(captured.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("createTransaction: When account does not belong to user, should throw ResourceNotFoundException and abort")
    void createTransaction_WhenAccountNotOwned_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long accountId = 999L;
        Long categoryId = 10L;

        doThrow(new ResourceNotFoundException("Account not found"))
                .when(accountService).validateOwnership(accountId, userId);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.createTransaction(
                userId, accountId, categoryId, TransactionType.INCOME, new BigDecimal("100.00"), "Bonus"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account not found");

        verify(accountService).validateOwnership(accountId, userId);
        verifyNoInteractions(transactionCategoryRepository);
        verify(accountService, never()).adjustBalance(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTransaction: When category not found or not visible to user, should throw ResourceNotFoundException")
    void createTransaction_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long userId = 1L;
        Long accountId = 10L;
        Long categoryId = 999L;

        doNothing().when(accountService).validateOwnership(accountId, userId);
        when(transactionCategoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.createTransaction(
                userId, accountId, categoryId, TransactionType.EXPENSE, new BigDecimal("50.00"), "Lunch"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found.");

        verify(accountService).validateOwnership(accountId, userId);
        verify(transactionCategoryRepository).findByIdAndUserId(categoryId, userId);
        verify(accountService, never()).adjustBalance(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAccountTransactions: When account is owned by user, should return transactions list")
    void getAccountTransactions_WhenOwned_ShouldReturnList() {
        // Arrange
        Long userId = 1L;
        Long accountId = 10L;
        List<Transaction> expected = List.of(
                new Transaction(2L, accountId, 1L, TransactionType.EXPENSE, new BigDecimal("20.00"), "Coffee", OffsetDateTime.now()),
                new Transaction(1L, accountId, 2L, TransactionType.INCOME, new BigDecimal("1000.00"), "Salary", OffsetDateTime.now().minusDays(1))
        );

        doNothing().when(accountService).validateOwnership(accountId, userId);
        when(transactionRepository.findAllByAccountId(accountId)).thenReturn(expected);

        // Act
        List<Transaction> result = transactionService.getAccountTransactions(userId, accountId);

        // Assert
        assertThat(result).hasSize(2).isEqualTo(expected);
        verify(accountService).validateOwnership(accountId, userId);
        verify(transactionRepository).findAllByAccountId(accountId);
    }

    @Test
    @DisplayName("getAccountTransactions: When account is not owned by user, should throw ResourceNotFoundException")
    void getAccountTransactions_WhenNotOwned_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        Long accountId = 99L;
        doThrow(new ResourceNotFoundException("Account not found"))
                .when(accountService).validateOwnership(accountId, userId);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.getAccountTransactions(userId, accountId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(accountService).validateOwnership(accountId, userId);
        verifyNoInteractions(transactionRepository);
    }
}
