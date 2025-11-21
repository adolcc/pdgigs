package com.pdgigs.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeRoleRequest(
        @NotBlank(message = "Role is required")
        String role
) {}