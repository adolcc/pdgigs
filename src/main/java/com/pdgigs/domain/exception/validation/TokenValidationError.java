package com.pdgigs.domain.exception.validation;

public sealed interface TokenValidationError
        permits TokenValidationError.Invalid,
        TokenValidationError.Expired,
        TokenValidationError.Missing,
        TokenValidationError.SignatureInvalid {

    String field();
    String message();

    default ValidationException toException() {
        return new ValidationException(field(), message());
    }

    record Invalid() implements TokenValidationError {
        @Override
        public String field() {
            return "token";
        }

        @Override
        public String message() {
            return "Invalid or malformed token";
        }
    }

    record Expired() implements TokenValidationError {
        @Override
        public String field() {
            return "token";
        }

        @Override
        public String message() {
            return "Token has expired";
        }
    }

    record Missing() implements TokenValidationError {
        @Override
        public String field() {
            return "token";
        }

        @Override
        public String message() {
            return "Authentication token is required";
        }
    }

    record SignatureInvalid() implements TokenValidationError {
        @Override
        public String field() {
            return "token";
        }

        @Override
        public String message() {
            return "Token signature is invalid";
        }
    }
}