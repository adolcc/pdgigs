package com.pdgigs.infrastructure.adapter.input.rest.exception;

import com.pdgigs.domain.exception.FileSizeExceededException;
import com.pdgigs.domain.exception.InvalidFileFormatException;
import com.pdgigs.domain.exception.ScoreNotFoundException;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(WebExchangeBindException ex) {
        String defaultMessage = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Validation error";
        log.error("Validation error: {}", defaultMessage);

        ErrorResponse errorResponse = new ErrorResponse(
                defaultMessage,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileFormat(InvalidFileFormatException ex) {
        log.error("Invalid file format error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(errorResponse);
    }

    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileSizeExceeded(FileSizeExceededException ex) {
        log.error("File size exceeded error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(errorResponse);
    }

    @ExceptionHandler(ScoreNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScoreNotFound(ScoreNotFoundException ex) {
        log.error("Score not found error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}