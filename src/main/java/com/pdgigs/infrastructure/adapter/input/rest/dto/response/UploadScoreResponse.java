package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

import java.time.LocalDateTime;

public record UploadScoreResponse(
        String id,
        String title,
        String author,
        String musicStyle,
        String filename,
        String userEmail,
        LocalDateTime createdAt
) { }