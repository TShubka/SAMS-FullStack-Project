package com.group6.sams.mapper;

import com.group6.sams.dto.response.UserResponse;
import com.group6.sams.entity.Role;
import com.group6.sams.entity.User;

import java.util.List;

/**
 * Entity to DTO conversion. Plain Java rather than MapStruct - MapStruct is not in
 * the approved technology stack, and hand-written mappers are trivially explainable
 * in the viva.
 *
 * Must be called inside the service's transaction, so lazy associations are still
 * attached to a live persistence context.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .sorted()
                .toList();

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
