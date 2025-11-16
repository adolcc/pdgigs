package com.pdgigs.infrastructure.adapter.input.rest.exception.handler;

import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ErrorResponse;
import com.pdgigs.infrastructure.adapter.input.rest.exception.mapper.ErrorResponseMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
@Order(2)
public class ValidationExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleConstraintViolation(
            ConstraintViolationException ex,
            ServerWebExchange exchange
    ) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Validation error");

        log.error("Constraint violation: {}", message);

        ErrorResponse error = ErrorResponseMapper.fromValidationError(
                message,
                HttpStatus.BAD_REQUEST,
                exchange
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebExchangeBindException(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        String message = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.error("Binding error: {}", message);

        ErrorResponse error = ErrorResponseMapper.fromValidationError(
                message,
                HttpStatus.BAD_REQUEST,
                exchange
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }
}