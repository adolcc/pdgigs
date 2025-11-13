package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

public record ScoreResponse (
        String id,
        String title,
        String author,
        String musicalStyle,
        Long fileSize
) { }