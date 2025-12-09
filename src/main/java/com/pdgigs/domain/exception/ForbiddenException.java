package com.pdgigs.domain.exception;

public class ForbiddenException extends DomainException {

    private ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }

    public static ForbiddenException forbidden(String reason) {
        return new ForbiddenException("Forbidden: " + reason);
    }

    public static ForbiddenException modifyUserNotAllowed(String callerId, String targetId) {
        return new ForbiddenException("User '" + callerId + "' is not allowed to modify user '" + targetId + "'");
    }
}