package com.pdgigs.infrastructure.adapter.input.rest.helper;

import com.pdgigs.domain.model.Score;

public class ScoreMockFactory {

    public static Score createWithCompleteMetadata(String id) {
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();
        return Score.builder()
                .id(id)
                .title("Concierto Nº 5")
                .author("Mozart")
                .musicalStyle("Clásico")
                .pdfContent(pdfContent)
                .fileSize((long) pdfContent.length)
                .build();
    }

    public static Score createWithEmptyMetadata(String id) {
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();
        return Score.builder()
                .id(id)
                .title("")
                .author("")
                .musicalStyle("")
                .pdfContent(pdfContent)
                .fileSize((long) pdfContent.length)
                .build();
    }

    public static Score createWithPartialMetadata(String id, String title) {
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();
        return Score.builder()
                .id(id)
                .title(title)
                .author("")
                .musicalStyle("")
                .pdfContent(pdfContent)
                .fileSize((long) pdfContent.length)
                .build();
    }

    public static Score create(String id, String title, String author, String musicalStyle) {
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();
        return Score.builder()
                .id(id)
                .title(title)
                .author(author)
                .musicalStyle(musicalStyle)
                .pdfContent(pdfContent)
                .fileSize((long) pdfContent.length)
                .build();
    }
}