package com.pdgigs.infrastructure.adapter.input.rest.exception.handler;

import com.pdgigs.domain.exception.*;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ErrorResponse;
import com.pdgigs.infrastructure.adapter.input.rest.exception.mapper.ErrorResponseMapper;
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
            ResourceNotFoundException ex, ServerWebExchange exchange) {
        log.warn("Resource not found: {}", ex.getMessage());
        return createResponse(ex, HttpStatus.NOT_FOUND, exchange);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnauthorized(
            UnauthorizedException ex, ServerWebExchange exchange) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return createResponse(ex, HttpStatus.UNAUTHORIZED, exchange);
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            ValidationException ex, ServerWebExchange exchange) {
        log.warn("Validation error: {}", ex.getMessage());
        return createResponse(ex, HttpStatus.BAD_REQUEST, exchange);
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleConflict(
            ConflictException ex, ServerWebExchange exchange) {
        log.warn("Conflict: {}", ex.getMessage());
        return createResponse(ex, HttpStatus.CONFLICT, exchange);
    }

    private Mono<ResponseEntity<ErrorResponse>> createResponse(
            DomainException ex, HttpStatus status, ServerWebExchange exchange) {
        ErrorResponse error = ErrorResponseMapper.fromDomainException(ex, status, exchange);
        return Mono.just(ResponseEntity.status(status).body(error));
    }
}