package com.pdgigs.domain.exception.validation;

public sealed interface ScoreValidationError
        permits ScoreValidationError.InvalidScoreId,
        ScoreValidationError.TitleCannotBeBlank,
        ScoreValidationError.TitleTooLong,
        ScoreValidationError.AuthorCannotBeBlank,
        ScoreValidationError.AuthorTooLong,
        ScoreValidationError.MusicalStyleCannotBeBlank,
        ScoreValidationError.MusicalStyleTooLong {

    String field();
    String message();

    default ValidationException toException() {
        return new ValidationException(field(), message());
    }

    record InvalidScoreId() implements ScoreValidationError {
        @Override
        public String field() {
            return "scoreId";
        }

        @Override
        public String message() {
            return "Invalid score ID format. Must be a valid MongoDB ObjectId (24 hexadecimal characters)";
        }
    }

    record TitleCannotBeBlank() implements ScoreValidationError {
        @Override
        public String field() {
            return "title";
        }

        @Override
        public String message() {
            return "Title cannot be blank";
        }
    }

    record TitleTooLong(int actualLength, int maxLength) implements ScoreValidationError {
        @Override
        public String field() {
            return "title";
        }

        @Override
        public String message() {
            return String.format("Title is too long (%d characters). Maximum allowed is %d characters",
                    actualLength, maxLength);
        }
    }

    record AuthorCannotBeBlank() implements ScoreValidationError {
        @Override
        public String field() {
            return "author";
        }

        @Override
        public String message() {
            return "Author cannot be blank";
        }
    }

    record AuthorTooLong(int actualLength, int maxLength) implements ScoreValidationError {
        @Override
        public String field() {
            return "author";
        }

        @Override
        public String message() {
            return String.format("Author is too long (%d characters). Maximum allowed is %d characters",
                    actualLength, maxLength);
        }
    }

    record MusicalStyleCannotBeBlank() implements ScoreValidationError {
        @Override
        public String field() {
            return "musicalStyle";
        }

        @Override
        public String message() {
            return "Musical style cannot be blank";
        }
    }

    record MusicalStyleTooLong(int actualLength, int maxLength) implements ScoreValidationError {
        @Override
        public String field() {
            return "musicalStyle";
        }

        @Override
        public String message() {
            return String.format("Musical style is too long (%d characters). Maximum allowed is %d characters",
                    actualLength, maxLength);
        }
    }
}