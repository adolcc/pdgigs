package com.pdgigs.domain.exception.validation;

public sealed interface FileValidationError
        permits FileValidationError.InvalidFormat,
        FileValidationError.Empty,
        FileValidationError.SizeExceeded {

    String field();
    String message();

    default ValidationException toException() {
        return new ValidationException(field(), message());
    }

    record InvalidFormat() implements FileValidationError {
        @Override
        public String field() {
            return "file";
        }

        @Override
        public String message() {
            return "Invalid file format. Only PDF files are allowed";
        }
    }

    record Empty() implements FileValidationError {
        @Override
        public String field() {
            return "file";
        }

        @Override
        public String message() {
            return "File cannot be empty";
        }
    }

    record SizeExceeded(long actualSize, long maxSize) implements FileValidationError {
        @Override
        public String field() {
            return "file";
        }

        @Override
        public String message() {
            return String.format("File size (%d bytes) exceeds maximum allowed (%d bytes)", actualSize, maxSize);
        }
    }
}