package com.group6.sams.service;

import com.group6.sams.dto.request.LoginRequest;
import com.group6.sams.dto.request.RegisterRequest;
import com.group6.sams.dto.response.JwtResponse;
import com.group6.sams.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    JwtResponse login(LoginRequest request);

    UserResponse getCurrentUser(Long userId);
}
