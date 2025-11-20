package com.pdgigs.domain.exception.validation;

public sealed interface UserValidationError
        permits UserValidationError.EmailRequired,
        UserValidationError.InvalidEmail,
        UserValidationError.EmailAlreadyExists,
        UserValidationError.PasswordRequired,
        UserValidationError.PasswordTooShort,
        UserValidationError.InvalidCredentials {

    String field();
    String message();

    default ValidationException toException() {
        return new ValidationException(field(), message());
    }

    record EmailRequired() implements UserValidationError {
        @Override
        public String field() {
            return "email";
        }

        @Override
        public String message() {
            return "Email is required";
        }
    }

    record InvalidEmail() implements UserValidationError {
        @Override
        public String field() {
            return "email";
        }

        @Override
        public String message() {
            return "Invalid email format";
        }
    }

    record EmailAlreadyExists(String email) implements UserValidationError {
        @Override
        public String field() {
            return "email";
        }

        @Override
        public String message() {
            return String.format("Email already exists: %s", email);
        }
    }

    record PasswordRequired() implements UserValidationError {
        @Override
        public String field() {
            return "password";
        }

        @Override
        public String message() {
            return "Password is required";
        }
    }

    record PasswordTooShort() implements UserValidationError {
        @Override
        public String field() {
            return "password";
        }

        @Override
        public String message() {
            return "Password must be at least 8 characters long";
        }
    }

    record InvalidCredentials() implements UserValidationError {
        @Override
        public String field() {
            return "credentials";
        }

        @Override
        public String message() {
            return "Invalid email or password";
        }
    }
}