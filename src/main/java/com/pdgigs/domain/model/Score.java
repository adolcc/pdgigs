package com.pdgigs.domain.model;

public record Score(
        String id,
        String title,
        String author,
        String musicalStyle,
        byte[]pdfContent,
        Long fileSize
) { }