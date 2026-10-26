package com.financetracker.user.infrastructure.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("UserRepository JPA Integration Tests")
class UserRepositoryIntegrationTest {

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    @DisplayName("save & findByEmail: Should persist user entity and query by email")
    void saveAndFindByEmail_WhenValid_ShouldPersistAndFind() {
        // Arrange
        UserJpaEntity entity = new UserJpaEntity();
        entity.setEmail("integration@user.com");
        entity.setPassword("$2a$10$dummyHash");
        entity.setFullName("Integration User");

        // Act
        UserJpaEntity saved = jpaUserRepository.save(entity);
        Optional<UserJpaEntity> found = jpaUserRepository.findByEmail("integration@user.com");

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("integration@user.com");
        assertThat(found.get().getFullName()).isEqualTo("Integration User");
    }

    @Test
    @DisplayName("save: When email is duplicate, should violate unique constraint and throw exception")
    void save_WhenDuplicateEmail_ShouldThrowDataIntegrityViolationException() {
        // Arrange
        UserJpaEntity user1 = new UserJpaEntity();
        user1.setEmail("duplicate@example.com");
        user1.setPassword("hash1");
        user1.setFullName("User One");
        jpaUserRepository.saveAndFlush(user1);

        UserJpaEntity user2 = new UserJpaEntity();
        user2.setEmail("duplicate@example.com");
        user2.setPassword("hash2");
        user2.setFullName("User Two");

        // Act & Assert
        assertThatThrownBy(() -> jpaUserRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
