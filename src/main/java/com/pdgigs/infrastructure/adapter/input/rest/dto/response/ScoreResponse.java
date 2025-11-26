package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

public record ScoreResponse(
        String id,
        String title,
        String author,
        String musicalStyle,
        Long fileSize,
        String pdfDownloadUrl,
        String userId,
        String userEmail
) {
    public ScoreResponse(String id, String title, String author, String musicalStyle,
                         Long fileSize, String pdfDownloadUrl) {
        this(id, title, author, musicalStyle, fileSize, pdfDownloadUrl, null, null);
    }
}