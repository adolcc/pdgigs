package com.pdgigs.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateNameRequest(
        @NotBlank(message = "Name is required")
        String name
) {}