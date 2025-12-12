package com.pdgigs.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateScoreRequest(
        @Size(max = 200) String title,
        @Size(max = 200) String author,
        @Size(max = 100) String musicStyle
) {}