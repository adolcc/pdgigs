package com.pdgigs.domain.exception.validation;

import com.pdgigs.domain.exception.DomainException;

public class ValidationException extends DomainException {

    private final String field;

    public ValidationException(String field, String reason) {
        super("VALIDATION_ERROR", String.format("Validation failed for '%s': %s", field, reason));
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public static ValidationException invalidField(String field, String reason) {
        return new ValidationException(field, reason);
    }

    public static ValidationException required(String field) {
        return new ValidationException(field, "is required and cannot be empty");
    }
}