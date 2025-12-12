package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

import java.time.LocalDateTime;

public record ScoreResponse(
        String id,
        String title,
        String author,
        String musicStyle,
        String filename,
        LocalDateTime createAT
) {
    public ScoreResponse(String id, String title, String author, String musicStyle, String filename, LocalDateTime createAT) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.musicStyle = musicStyle;
        this.filename = filename;
        this.createAT = createAT;
    }
}

