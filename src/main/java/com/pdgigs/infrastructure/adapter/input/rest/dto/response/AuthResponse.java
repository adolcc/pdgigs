package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

public record AuthResponse(
        String token,
        String email,
        String name,
        String role
) {}