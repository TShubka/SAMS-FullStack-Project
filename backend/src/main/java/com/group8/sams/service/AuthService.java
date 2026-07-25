package com.group8.sams.service;

import com.group8.sams.dto.request.LoginRequest;
import com.group8.sams.dto.request.RegisterRequest;
import com.group8.sams.dto.response.JwtResponse;
import com.group8.sams.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    JwtResponse login(LoginRequest request);

    UserResponse getCurrentUser(Long userId);
}
