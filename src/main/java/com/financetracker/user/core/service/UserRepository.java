package com.financetracker.user.core.service;

import com.financetracker.user.core.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(User user);
}
