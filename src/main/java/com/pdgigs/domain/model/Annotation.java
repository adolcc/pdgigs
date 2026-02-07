package com.pdgigs.domain.model;

import java.time.LocalDateTime;

public record Annotation(
        String id,
        String scoreId,
        Integer pageNumber,
        String annotationsJson,
        LocalDateTime updatedAt,
        String updatedBy
) {
    public Annotation(String id, String scoreId, Integer pageNumber, String annotationsJson, String updatedBy) {
        this(id,
                scoreId,
                pageNumber,
                annotationsJson == null ? "" : annotationsJson,
                LocalDateTime.now(),
                updatedBy);
    }

    public Annotation {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (annotationsJson == null) annotationsJson = "";
    }
}