package com.pdgigs.infrastructure.adapter.input.rest.helper;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

public class MultipartRequestFactory {

    public static MultipartBodyBuilder createWithCompleteMetadata() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        byte[] validPdf = PdfContentFactory.createValidPdfContent();

        builder.part("file", new ByteArrayResource(validPdf) {
            @Override
            public String getFilename() {
                return "test-score.pdf";
            }
        });
        builder.part("title", "Concierto No. 5");
        builder.part("author", "Mozart");
        builder.part("musicalStyle", "Clásico");

        return builder;
    }

    public static MultipartBodyBuilder createWithInvalidFormat() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        byte[] invalidFile = "This is not a PDF".getBytes();

        builder.part("file", new ByteArrayResource(invalidFile) {
            @Override
            public String getFilename() {
                return "invalid.txt";
            }
        });
        builder.part("title", "Test");
        builder.part("author", "Test");
        builder.part("musicalStyle", "Test");

        return builder;
    }

    public static MultipartBodyBuilder createWithLargeFile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        byte[] largePdf = new byte[11 * 1024 * 1024]; // 11 MB
        largePdf[0] = '%';
        largePdf[1] = 'P';
        largePdf[2] = 'D';
        largePdf[3] = 'F';

        builder.part("file", new ByteArrayResource(largePdf) {
            @Override
            public String getFilename() {
                return "large-score.pdf";
            }
        });
        builder.part("title", "Test");
        builder.part("author", "Test");
        builder.part("musicalStyle", "Test");

        return builder;
    }

    public static MultipartBodyBuilder createWithEmptyFile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        byte[] emptyFile = new byte[0];

        builder.part("file", new ByteArrayResource(emptyFile) {
            @Override
            public String getFilename() {
                return "empty.pdf";
            }
        });
        builder.part("title", "Test");
        builder.part("author", "Test");
        builder.part("musicalStyle", "Test");

        return builder;
    }
}