package com.pdgigs.domain.validator;

import com.pdgigs.domain.exception.ValidationException;
import reactor.core.publisher.Mono;

public class ScoreValidator {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_AUTHOR_LENGTH = 100;
    private static final int MAX_STYLE_LENGTH = 50;

    public static Mono<Void> validateScoreId(String scoreId) {
        if (scoreId == null || scoreId.isEmpty()) {
            return Mono.error(ValidationException.required("scoreId"));
        }

        if (!scoreId.matches("^[a-fA-F0-9]{24}$")) {
            String reason = "Invalid score ID format. Must be a valid MongoDB ObjectId (24 hexadecimal characters)";
            return Mono.error(ValidationException.invalidField("scoreId", reason));
        }

        return Mono.empty();
    }

    public static Mono<Void> validateMetadata(String title, String author, String musicStyle) {

        if (title != null && !title.isEmpty() && title.isBlank()) {
            return Mono.error(ValidationException.required("title"));
        }
        if (title != null && title.length() > MAX_TITLE_LENGTH) {
            String reason = String.format("Title is too long (%d characters). Maximum allowed is %d characters",
                    title.length(), MAX_TITLE_LENGTH);
            return Mono.error(ValidationException.invalidField("title", reason));
        }

        if (author != null && !author.isEmpty() && author.isBlank()) {
            return Mono.error(ValidationException.required("author"));
        }
        if (author != null && author.length() > MAX_AUTHOR_LENGTH) {
            String reason = String.format("Author is too long (%d characters). Maximum allowed is %d characters",
                    author.length(), MAX_AUTHOR_LENGTH);
            return Mono.error(ValidationException.invalidField("author", reason));
        }

        if (musicStyle != null && !musicStyle.isEmpty() && musicStyle.isBlank()) {
            return Mono.error(ValidationException.required("musicStyle"));
        }
        if (musicStyle != null && musicStyle.length() > MAX_STYLE_LENGTH) {
            String reason = String.format("Musical style is too long (%d characters). Maximum allowed is %d characters",
                    musicStyle.length(), MAX_STYLE_LENGTH);
            return Mono.error(ValidationException.invalidField("musicStyle", reason));
        }

        return Mono.empty();
    }
}