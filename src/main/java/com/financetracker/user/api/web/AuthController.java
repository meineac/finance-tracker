package com.financetracker.user.api.web;

import com.financetracker.user.core.model.User;
import com.financetracker.user.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user management and session authorization")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register a new application user profile")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = authService.register(
                request.email(),
                request.password(),
                request.fullName()
        );

        String token = authService.login(registeredUser.email(), request.password());

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token));
    }

    @Operation(summary = "Login into application user profile")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
