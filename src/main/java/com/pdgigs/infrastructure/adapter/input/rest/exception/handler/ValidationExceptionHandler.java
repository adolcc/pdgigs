package com.pdgigs.infrastructure.adapter.input.rest.exception.handler;

import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ErrorResponse;
import com.pdgigs.infrastructure.adapter.input.rest.exception.mapper.ErrorResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Order(2)
public class ValidationExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebExchangeBindException(
            WebExchangeBindException ex, ServerWebExchange exchange) {

        String message = ex.getFieldErrors().stream()
                .map(error -> String.format("Field '%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        log.warn("DTO Binding error: {}", message);

        return Mono.fromCallable(() -> ErrorResponseMapper.fromValidationError(
                message, HttpStatus.BAD_REQUEST, exchange
        )).map(response -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }
}