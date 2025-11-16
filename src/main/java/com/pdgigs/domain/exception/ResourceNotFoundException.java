package com.pdgigs.domain.exception;

public class ResourceNotFoundException extends DomainException {

    private ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }

    public static ResourceNotFoundException score(String id) {
        return new ResourceNotFoundException("Score not found with ID: " + id);
    }

    public static ResourceNotFoundException user(String email) {
        return new ResourceNotFoundException("User not found with email: " + email);
    }

    public static ResourceNotFoundException userById(String id) {
        return new ResourceNotFoundException("User not found with ID: " + id);
    }
}