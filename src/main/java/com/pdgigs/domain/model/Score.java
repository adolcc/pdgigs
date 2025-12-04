package com.pdgigs.domain.model;

import java.time.LocalDateTime;

public record Score(
        String id,
        String title,
        String author,
        String musicStyle,
        String filename,
        LocalDateTime createdAt
) {
    public Score(String id, String title, String author, String musicStyle, String filename) {
        this(id, title == null ? "" : title, author == null ? "" : author, musicStyle == null ? "" : musicStyle, filename, LocalDateTime.now());
    }

    public Score {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (title == null) title = "";
        if (author == null) author = "";
        if (musicStyle == null) musicStyle = "";
    }
}
