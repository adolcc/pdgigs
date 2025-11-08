package com.pdgigs.infrastructure.adapter.input.rest.helper;

public class PdfContentFactory {

    private static final String PDF_HEADER = "%PDF-1.4";
    private static final int MB = 1024 * 1024;

    public static byte[] createValidPdfContent() {
        return (PDF_HEADER + "\nfake-pdf-content").getBytes();
    }

    public static byte[] createValidPdfContent(int sizeInKB) {
        StringBuilder content = new StringBuilder(PDF_HEADER);
        int remainingSize = (sizeInKB * 1024) - PDF_HEADER.length();

        for (int i = 0; i < remainingSize; i++) {
            content.append('x');
        }
        return content.toString().getBytes();
    }

    public static byte[] createLargePdfContent() {
        byte[] largePdfContent = new byte[11 * MB];
        largePdfContent[0] = '%';
        largePdfContent[1] = 'P';
        largePdfContent[2] = 'D';
        largePdfContent[3] = 'F';
        return largePdfContent;
    }

    public static byte[] createInvalidContent() {
        return "not-a-pdf-content".getBytes();
    }
}