package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

public record UserResponse(
        String id,
        String email,
        String name,
        String role
) {}