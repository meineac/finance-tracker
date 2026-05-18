package com.financetracker.user.core.service;

import com.financetracker.shared.core.exception.DuplicateResourceException;
import com.financetracker.shared.core.exception.UnauthorizedAccessException;
import com.financetracker.user.core.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Override
    @Transactional
    public User register(String email, String password, String fullName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(null, email, hashedPassword, fullName);

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.password())) {
            throw new UnauthorizedAccessException("Invalid credentials");
        }

        return tokenProvider.generateToken(user);
    }
}
