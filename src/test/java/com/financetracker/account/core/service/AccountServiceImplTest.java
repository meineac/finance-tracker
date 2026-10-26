package com.financetracker.account.core.service;

import com.financetracker.account.core.model.Account;
import com.financetracker.account.core.model.AccountType;
import com.financetracker.shared.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl Unit Tests")
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @ParameterizedTest(name = "Account type = {0}")
    @EnumSource(AccountType.class)
    @DisplayName("createAccount: Should initialize account with ZERO balance and save via repository")
    void createAccount_ShouldInitializeWithZeroBalanceAndSave(AccountType type) {
        // Arrange
        Long userId = 42L;
        String name = "My " + type.name() + " Account";
        Account savedAccount = new Account(1L, userId, name, type, BigDecimal.ZERO);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        // Act
        Account result = accountService.createAccount(userId, name, type);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo(name);
        assertThat(result.type()).isEqualTo(type);
        assertThat(result.balance()).isEqualTo(BigDecimal.ZERO);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account captured = captor.getValue();
        assertThat(captured.balance()).isEqualTo(BigDecimal.ZERO);
        assertThat(captured.type()).isEqualTo(type);
        assertThat(captured.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("getUserAccounts: Should return list of accounts belonging to user")
    void getUserAccounts_ShouldReturnListOfAccounts() {
        // Arrange
        Long userId = 10L;
        List<Account> expectedAccounts = List.of(
                new Account(1L, userId, "Main Card", AccountType.CARD, new BigDecimal("1500.00")),
                new Account(2L, userId, "Cash Wallet", AccountType.CASH, new BigDecimal("200.00"))
        );
        when(accountRepository.findAllByUserId(userId)).thenReturn(expectedAccounts);

        // Act
        List<Account> actualAccounts = accountService.getUserAccounts(userId);

        // Assert
        assertThat(actualAccounts).hasSize(2).isEqualTo(expectedAccounts);
        verify(accountRepository).findAllByUserId(userId);
    }

    @Test
    @DisplayName("getByIdAndUserId: When account exists, should return account")
    void getByIdAndUserId_WhenExists_ShouldReturnAccount() {
        // Arrange
        Long accountId = 5L;
        Long userId = 10L;
        Account expectedAccount = new Account(accountId, userId, "Salary Card", AccountType.CARD, BigDecimal.ZERO);
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(expectedAccount));

        // Act
        Account actualAccount = accountService.getByIdAndUserId(accountId, userId);

        // Assert
        assertThat(actualAccount).isEqualTo(expectedAccount);
        verify(accountRepository).findByIdAndUserId(accountId, userId);
    }

    @Test
    @DisplayName("getByIdAndUserId: When account not found or belongs to another user, should throw ResourceNotFoundException")
    void getByIdAndUserId_WhenNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long accountId = 99L;
        Long userId = 10L;
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> accountService.getByIdAndUserId(accountId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account not found");

        verify(accountRepository).findByIdAndUserId(accountId, userId);
    }

    @Test
    @DisplayName("validateOwnership: When account belongs to user, should complete normally")
    void validateOwnership_WhenBelongsToUser_ShouldPass() {
        // Arrange
        Long accountId = 1L;
        Long userId = 2L;
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);

        // Act & Assert (should not throw)
        accountService.validateOwnership(accountId, userId);

        verify(accountRepository).existsByIdAndUserId(accountId, userId);
    }

    @Test
    @DisplayName("validateOwnership: When account does not belong to user, should throw ResourceNotFoundException")
    void validateOwnership_WhenNotBelongsToUser_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long accountId = 1L;
        Long userId = 2L;
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> accountService.validateOwnership(accountId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account not found");

        verify(accountRepository).existsByIdAndUserId(accountId, userId);
    }

    @Test
    @DisplayName("adjustBalance: Should delegate adjustment to repository")
    void adjustBalance_ShouldDelegateToRepository() {
        // Arrange
        Long accountId = 1L;
        BigDecimal delta = new BigDecimal("100.50");

        // Act
        accountService.adjustBalance(accountId, delta);

        // Assert
        verify(accountRepository).adjustBalance(accountId, delta);
    }
}
