package com.pdgigs.domain.exception;

public class UnauthorizedException extends DomainException {

    private static final String ERROR_CODE = "UNAUTHORIZED";

    private UnauthorizedException(String message) {
        super(ERROR_CODE, message);
    }

    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("Invalid email or password");
    }

    public static UnauthorizedException invalidToken() {
        return new UnauthorizedException("Invalid or missing authentication token");
    }
}
