package com.pdgigs.domain.model;

import java.time.LocalDateTime;

public record Score(
        String id,
        String title,
        String author,
        String musicalStyle,
        byte[] pdfContent,
        Long fileSize,
        String userId,
        String userEmail,
        LocalDateTime createdAt
) {
    public Score(String id, String title, String author, String musicalStyle,
                 byte[] pdfContent, Long fileSize) {
        this(id, title, author, musicalStyle, pdfContent, fileSize, null, null, LocalDateTime.now());
    }

    public Score {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}