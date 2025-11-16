package com.pdgigs.domain.exception;

public class ConflictException extends DomainException {

    private ConflictException(String message) {
        super("RESOURCE_CONFLICT", message);
    }

    public static ConflictException userAlreadyExists(String email) {
        return new ConflictException("User with email '" + email + "' already exists");
    }

    public static ConflictException scoreAlreadyExists(String id) {
        return new ConflictException("Score with ID '" + id + "' already exists");
    }

    public static ConflictException duplicateScore(String title, String author) {
        return new ConflictException("A score with title '" + title + "' by '" + author + "' already exists");
    }
}