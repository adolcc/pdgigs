package com.pdgigs.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}