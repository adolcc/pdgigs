package com.pdgigs.infrastructure.adapter.input.rest.exception.handler;

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
@Order(999)
public class GlobalFallbackHandler {

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {

        log.error("❌ UNEXPECTED ERROR: {}", ex.getMessage(), ex);

        HttpStatus status = (exchange.getResponse().getStatusCode() != null)
                ? HttpStatus.valueOf(exchange.getResponse().getStatusCode().value())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return Mono.fromCallable(() -> ErrorResponseMapper.fromGenericException(
                ex, status, exchange
        )).map(response -> ResponseEntity.status(status).body(response));
    }
}