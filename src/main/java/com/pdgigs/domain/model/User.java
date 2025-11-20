package com.pdgigs.domain.model;

import java.time.LocalDateTime;

public record User(
        String id,
        String email,
        String name,
        String password,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}
