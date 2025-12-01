package com.pdgigs.domain.validator;

import com.pdgigs.domain.exception.ValidationException;
import reactor.core.publisher.Mono;

public class FileValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String PDF_MAGIC_NUMBER = "%PDF";

    public static Mono<Void> validatePdfFormat(byte[] content) {
        if (content == null || content.length < 4) {
            return Mono.error(ValidationException.required("file"));
        }

        String header = new String(content, 0, Math.min(4, content.length));
        if (!header.startsWith(PDF_MAGIC_NUMBER)) {
            return Mono.error(ValidationException.invalidField("file", "Invalid file format. Only PDF files are allowed"));
        }

        return Mono.empty();
    }

    public static Mono<Void> validateFileSize(byte[] content) {
        if (content == null) {
            return Mono.error(ValidationException.required("file"));
        }

        if (content.length > MAX_FILE_SIZE) {
            String reason = String.format("File size (%d bytes) exceeds maximum allowed (%d bytes)", content.length, MAX_FILE_SIZE);
            return Mono.error(ValidationException.invalidField("file", reason));
        }

        return Mono.empty();
    }

    public static Mono<Void> validateFile(byte[] content) {
        return validateFileSize(content)
                .then(validatePdfFormat(content));
    }
}