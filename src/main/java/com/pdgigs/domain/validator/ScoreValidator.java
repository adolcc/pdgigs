package com.pdgigs.domain.validator;

import com.pdgigs.domain.exception.validation.ScoreValidationError;
import reactor.core.publisher.Mono;

public class ScoreValidator {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_AUTHOR_LENGTH = 100;
    private static final int MAX_STYLE_LENGTH = 50;

    public static Mono<Void> validateScoreId(String scoreId) {
        if (scoreId == null || scoreId.isEmpty()) {
            return Mono.error(new ScoreValidationError.InvalidScoreId().toException());
        }

        if (!scoreId.matches("^[a-fA-F0-9]{24}$")) {
            return Mono.error(new ScoreValidationError.InvalidScoreId().toException());
        }

        return Mono.empty();
    }

    public static Mono<Void> validateMetadata(String title, String author, String musicalStyle) {

        if (title != null && !title.isEmpty() && title.isBlank()) {
            return Mono.error(new ScoreValidationError.TitleCannotBeBlank().toException());
        }
        if (title != null && title.length() > MAX_TITLE_LENGTH) {
            return Mono.error(new ScoreValidationError.TitleTooLong(title.length(), MAX_TITLE_LENGTH).toException());
        }

        if (author != null && !author.isEmpty() && author.isBlank()) {
            return Mono.error(new ScoreValidationError.AuthorCannotBeBlank().toException());
        }
        if (author != null && author.length() > MAX_AUTHOR_LENGTH) {
            return Mono.error(new ScoreValidationError.AuthorTooLong(author.length(), MAX_AUTHOR_LENGTH).toException());
        }

        if (musicalStyle != null && !musicalStyle.isEmpty() && musicalStyle.isBlank()) {
            return Mono.error(new ScoreValidationError.MusicalStyleCannotBeBlank().toException());
        }
        if (musicalStyle != null && musicalStyle.length() > MAX_STYLE_LENGTH) {
            return Mono.error(new ScoreValidationError.MusicalStyleTooLong(musicalStyle.length(), MAX_STYLE_LENGTH).toException());
        }

        return Mono.empty();
    }
}