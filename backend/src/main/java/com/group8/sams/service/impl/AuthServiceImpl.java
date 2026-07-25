package com.group8.sams.service.impl;

import com.group8.sams.dto.request.LoginRequest;
import com.group8.sams.dto.request.RegisterRequest;
import com.group8.sams.dto.response.JwtResponse;
import com.group8.sams.dto.response.UserResponse;
import com.group8.sams.entity.Role;
import com.group8.sams.entity.User;
import com.group8.sams.entity.enums.RoleName;
import com.group8.sams.exception.BusinessRuleException;
import com.group8.sams.exception.DuplicateResourceException;
import com.group8.sams.exception.ResourceNotFoundException;
import com.group8.sams.mapper.UserMapper;
import com.group8.sams.repository.RoleRepository;
import com.group8.sams.repository.UserRepository;
import com.group8.sams.security.JwtTokenProvider;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Authentication business logic. Owner: Member 1.
 *
 * All of it lives here rather than in the controller: the controller's only job is
 * to accept the HTTP request, hand it over, and wrap the result in a ResponseEntity.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Pre-checked so the client gets a readable 409 rather than a raw constraint
        // violation. The unique constraints on the table remain the real guarantee.
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        RoleName requestedRole = resolveRole(request.getRole());

        // Privilege escalation guard: /api/auth/register is public, so allowing
        // self-registration as ADMIN would let anyone mint an administrator account.
        // Admin accounts are created by an existing admin through /api/users.
        if (requestedRole == RoleName.ROLE_ADMIN) {
            throw new BusinessRuleException(
                    "Administrator accounts cannot be self-registered. "
                    + "Ask an existing administrator to create the account.");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(findRole(requestedRole));
        // Every account also holds ROLE_USER, which backs endpoints available to any
        // authenticated user regardless of their domain role.
        roles.add(findRole(RoleName.ROLE_USER));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(roles)
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public JwtResponse login(LoginRequest request) {
        // Delegating to the AuthenticationManager means the BCrypt comparison, the
        // disabled-account check and the timing-safe failure path are all handled by
        // Spring Security rather than reimplemented here.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(principal);

        return JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(principal.getId())
                .username(principal.getUsername())
                .email(principal.getEmail())
                .roles(principal.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .sorted()
                        .toList())
                .expiresInMs(tokenProvider.getExpirationMs())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserMapper.toResponse(user);
    }

    private RoleName resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return RoleName.ROLE_STUDENT;
        }
        return RoleName.valueOf("ROLE_" + role.toUpperCase());
    }

    private Role findRole(RoleName name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role " + name + " is missing. Has the seeder run?"));
    }
}
