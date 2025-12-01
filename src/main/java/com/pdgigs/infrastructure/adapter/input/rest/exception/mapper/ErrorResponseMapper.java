package com.pdgigs.infrastructure.adapter.input.rest.exception.mapper;

import com.pdgigs.domain.exception.DomainException;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

public class ErrorResponseMapper {

    public static ErrorResponse fromDomainException(
            DomainException ex,
            HttpStatus status,
            ServerWebExchange exchange
    ) {
        String message = ex.getMessage();

        if (ex.getErrorCode().equals("VALIDATION_ERROR") && message.contains("MongoDB ObjectId")) {
            message = "Validation failed for 'scoreId': Invalid ID format.";
        }

        return new ErrorResponse(
                message,
                status.value(),
                LocalDateTime.now(),
                extractPath(exchange),
                ex.getErrorCode()
        );
    }

    public static ErrorResponse fromGenericException(
            Exception ex,
            HttpStatus status,
            ServerWebExchange exchange
    ) {
        return new ErrorResponse(
                "Internal server error",
                status.value(),
                LocalDateTime.now(),
                extractPath(exchange),
                "INTERNAL_ERROR"
        );
    }

    public static ErrorResponse fromValidationError(
            String message,
            HttpStatus status,
            ServerWebExchange exchange
    ) {
        return new ErrorResponse(
                message,
                status.value(),
                LocalDateTime.now(),
                extractPath(exchange),
                "VALIDATION_ERROR"
        );
    }

    private static String extractPath(ServerWebExchange exchange) {
        if (exchange == null) {
            return null;
        }

        try {
            return exchange.getRequest().getPath().value();
        } catch (Exception e) {
            return null;
        }
    }
}