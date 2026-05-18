package com.financetracker.user.core.service;

import com.financetracker.user.core.model.User;

public interface AuthService {
    User register(String email, String password, String fullName);
    String login(String email, String password);
}
