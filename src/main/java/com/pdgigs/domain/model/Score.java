package com.pdgigs.domain.model;

public record Score(
        String id,
        String title,
        String author,
        String musicalStyle,
        byte[]pdfContent,
        Long fileSize
) {
    // constructores y métodos de validación de negocio.
}