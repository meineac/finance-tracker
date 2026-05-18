package com.financetracker.account.infrastructure.repository;

import com.financetracker.account.core.model.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class AccountJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Getter
    @Setter
    Long userId;

    @Column(nullable = false)
    @Getter
    @Setter
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Getter
    @Setter
    AccountType type;

    @Column(nullable = false)
    @Getter
    @Setter
    BigDecimal balance;
}
