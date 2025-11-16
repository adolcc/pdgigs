package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp,
        String path,
        String errorCode
) {

    public ErrorResponse(String message, int status, LocalDateTime timestamp) {
        this(message, status, timestamp, null, null);
    }
}