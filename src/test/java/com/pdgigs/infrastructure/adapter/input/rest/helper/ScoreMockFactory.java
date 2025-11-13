package com.pdgigs.infrastructure.adapter.input.rest.helper;

import com.pdgigs.domain.model.Score;

public class ScoreMockFactory {

    public static Score createWithCompleteMetadata(String id) {
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();
        return new Score(
                id,
                "Concierto Nº 5",
                "Mozart",
                "Clásico",
                pdfContent,
                (long) pdfContent.length
        );
    }

    public static Score createWithEmptyMetadata(String id) {
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();
        return new Score(
                id,
                "",
                "",
                "",
                pdfContent,
                (long) pdfContent.length
        );
    }

    public static Score createWithPartialMetadata(String id, String title) {
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();
        return new Score(
                id,
                title,
                "",
                "",
                pdfContent,
                (long) pdfContent.length
        );
    }

    public static Score create(String id, String title, String author, String musicalStyle) {
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();
        return new Score(
                id,
                title,
                author,
                musicalStyle,
                pdfContent,
                (long) pdfContent.length
        );
    }
}