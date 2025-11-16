package com.pdgigs.domain.validator;

import com.pdgigs.domain.exception.validation.UserValidationError;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final int MIN_PASSWORD_LENGTH = 8;

    public static Mono<Void> validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return Mono.error(new UserValidationError.EmailRequired().toException());
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return Mono.error(new UserValidationError.InvalidEmail().toException());
        }

        return Mono.empty();
    }

    public static Mono<Void> validatePassword(String password) {
        if (password == null || password.isBlank()) {
            return Mono.error(new UserValidationError.PasswordRequired().toException());
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return Mono.error(new UserValidationError.PasswordTooShort().toException());
        }

        return Mono.empty();
    }

    public static Mono<Void> validateCredentials(String email, String password) {
        return validateEmail(email)
                .then(validatePassword(password));
    }
}