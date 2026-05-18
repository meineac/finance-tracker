package com.financetracker.user.core.service;

import com.financetracker.user.core.model.User;

public interface TokenProvider {
    String generateToken(User user);
}
