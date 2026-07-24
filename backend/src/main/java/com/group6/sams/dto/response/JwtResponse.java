package com.group6.sams.dto.response;

import lombok.*;

import java.util.List;

/** Login result. Contains the token and just enough identity for the UI to render. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long userId;
    private String username;
    private String email;
    private List<String> roles;
    private long expiresInMs;
}
