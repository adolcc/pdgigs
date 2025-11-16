package com.pdgigs.infrastructure.adapter.input.rest.exception.handler;

import com.pdgigs.domain.exception.ConflictException;
import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.exception.validation.ValidationException;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ErrorResponse;
import com.pdgigs.infrastructure.adapter.input.rest.exception.mapper.ErrorResponseMapper;
import com.pdgigs.infrastructure.adapter.input.rest.exception.mapper.HttpStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
@Order(1)
public class DomainExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResourceNotFound(
            ResourceNotFoundException ex,
            ServerWebExchange exchange
    ) {
        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponseMapper.fromDomainException(
                ex,
                HttpStatus.NOT_FOUND,
                exchange
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            ValidationException ex,
            ServerWebExchange exchange
    ) {
        log.error("ValidationException caught: {}", ex.getMessage());
        log.error("Exception class: {}", ex.getClass().getName());

        HttpStatus status = HttpStatusMapper.mapValidationException(ex);

        log.error("Mapped to status: {}", status);

        ErrorResponse error = ErrorResponseMapper.fromDomainException(ex, status, exchange);

        return Mono.just(ResponseEntity.status(status).body(error));
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleConflict(
            ConflictException ex,
            ServerWebExchange exchange
    ) {
        log.error("Conflict error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponseMapper.fromDomainException(
                ex,
                HttpStatus.CONFLICT,
                exchange
        );

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }
}