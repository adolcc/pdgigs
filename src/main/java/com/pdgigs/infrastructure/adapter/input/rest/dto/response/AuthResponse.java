package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

import com.pdgigs.domain.model.User;

public record AuthResponse(
        String token,
        String email,
        String name,
        String role
) {

    public AuthResponse(String token) {
        this(token, null, null, null);
    }

    public static AuthResponse fromTokenAndUser(String token, User user) {
        if (user == null) return new AuthResponse(token);
        return new AuthResponse(token, user.email(), user.name(), user.role());
    }
}