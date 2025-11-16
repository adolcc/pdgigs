package com.pdgigs.infrastructure.adapter.input.rest.exception.mapper;

import com.pdgigs.domain.exception.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class HttpStatusMapper {

    public static HttpStatus mapValidationException(ValidationException ex) {
        String message = ex.getMessage().toLowerCase();

        log.debug("Mapping ValidationException with message: {}", message);

        if (message.contains("exceeds") ||
                message.contains("maximum") ||
                message.contains("file size") ||
                message.contains("too large")) {
            log.debug("Mapped to PAYLOAD_TOO_LARGE (413)");
            return HttpStatus.PAYLOAD_TOO_LARGE;
        }

        if (message.contains("invalid file format") ||
                message.contains("only pdf") ||
                message.contains("file format") ||
                message.contains("not allowed")) {
            log.debug("Mapped to UNSUPPORTED_MEDIA_TYPE (415)");
            return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        }

        if (message.contains("empty") || message.contains("cannot be empty")) {
            log.debug("Mapped to BAD_REQUEST (400)");
            return HttpStatus.BAD_REQUEST;
        }

        if (message.contains("invalid email or password") ||
                message.contains("credentials")) {
            log.debug("Mapped to UNAUTHORIZED (401)");
            return HttpStatus.UNAUTHORIZED;
        }

        if (message.contains("token") &&
                (message.contains("invalid") || message.contains("expired"))) {
            log.debug("Mapped to UNAUTHORIZED (401)");
            return HttpStatus.UNAUTHORIZED;
        }

        log.debug("Mapped to BAD_REQUEST (400) - default");
        return HttpStatus.BAD_REQUEST;
    }
}