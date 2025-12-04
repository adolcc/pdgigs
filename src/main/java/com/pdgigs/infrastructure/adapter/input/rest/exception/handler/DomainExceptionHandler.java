package com.pdgigs.infrastructure.adapter.input.rest.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdgigs.domain.exception.*;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ErrorResponse;
import com.pdgigs.infrastructure.adapter.input.rest.exception.mapper.ErrorResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
@Order(1)
public class DomainExceptionHandler {

    private final ObjectMapper objectMapper;
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    public DomainExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<Void> handleResourceNotFound(ResourceNotFoundException ex, ServerWebExchange exchange) {
        log.warn("Resource not found: {}", ex.getMessage());
        return writeErrorResponse(ex, HttpStatus.NOT_FOUND, exchange);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<Void> handleUnauthorized(UnauthorizedException ex, ServerWebExchange exchange) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return writeErrorResponse(ex, HttpStatus.UNAUTHORIZED, exchange);
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<Void> handleValidation(ValidationException ex, ServerWebExchange exchange) {
        log.warn("Validation error: {}", ex.getMessage());
        return writeErrorResponse(ex, HttpStatus.BAD_REQUEST, exchange);
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<Void> handleConflict(ConflictException ex, ServerWebExchange exchange) {
        log.warn("Conflict: {}", ex.getMessage());
        return writeErrorResponse(ex, HttpStatus.CONFLICT, exchange);
    }

    private Mono<Void> writeErrorResponse(DomainException ex, HttpStatus status, ServerWebExchange exchange) {
        try {
            ErrorResponse error = ErrorResponseMapper.fromDomainException(ex, status, exchange);
            byte[] bytes = objectMapper.writeValueAsBytes(error);

            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(status);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer buffer = bufferFactory.wrap(bytes);

            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }
}