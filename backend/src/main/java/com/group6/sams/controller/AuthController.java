package com.group6.sams.controller;

import com.group6.sams.dto.request.LoginRequest;
import com.group6.sams.dto.request.RegisterRequest;
import com.group6.sams.dto.response.JwtResponse;
import com.group6.sams.dto.response.UserResponse;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints. Owner: Member 1.
 *
 * Note what is absent: no business logic, no repository access, no try/catch. The
 * controller handles HTTP only - validation is declarative via @Valid, errors are
 * translated by GlobalExceptionHandler, and the work happens in AuthService.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 201 on success, 400 on validation failure, 409 if username or email is taken. */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** 200 with a token, 400 on validation failure, 401 on bad credentials. */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** The caller's own profile, resolved from the token rather than from a path id. */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> currentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getId()));
    }
}
