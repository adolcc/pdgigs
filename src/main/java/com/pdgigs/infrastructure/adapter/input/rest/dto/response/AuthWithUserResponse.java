package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

public record AuthWithUserResponse(
        String token,
        String tokenType,
        UserResponse user
) {}