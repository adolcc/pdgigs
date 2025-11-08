package com.pdgigs.infrastructure.adapter.input.rest.helper;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;

public class MultipartRequestFactory {

    public static MultipartBodyBuilder createWithCompleteMetadata() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        addPdfFile(builder, PdfContentFactory.createValidPdfContent(), "score.pdf");
        builder.part("title", "Concierto Nº 5");
        builder.part("author", "Mozart");
        builder.part("musicalStyle", "Clásico");
        return builder;
    }

    public static MultipartBodyBuilder createWithEmptyMetadata() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        addPdfFile(builder, PdfContentFactory.createValidPdfContent(), "score.pdf");
        return builder;
    }

    public static MultipartBodyBuilder createWithPartialMetadata(String title) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        addPdfFile(builder, PdfContentFactory.createValidPdfContent(), "score.pdf");
        builder.part("title", title);
        return builder;
    }

    public static MultipartBodyBuilder createWithInvalidFile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", PdfContentFactory.createInvalidContent())
                .filename("document.txt")
                .contentType(MediaType.TEXT_PLAIN);
        return builder;
    }

    public static MultipartBodyBuilder createWithLargeFile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        addPdfFile(builder, PdfContentFactory.createLargePdfContent(), "large-score.pdf");
        return builder;
    }

    private static void addPdfFile(MultipartBodyBuilder builder, byte[] content, String filename) {
        builder.part("file", content)
                .filename(filename)
                .contentType(MediaType.APPLICATION_PDF);
    }
}