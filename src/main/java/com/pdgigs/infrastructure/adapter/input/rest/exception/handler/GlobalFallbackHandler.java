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
            Exception ex,
            ServerWebExchange exchange
    ) {
        log.error("========================================");
        log.error("❌ CAUGHT BY GLOBAL FALLBACK HANDLER ❌");
        log.error("Exception type: {}", ex.getClass().getName());
        log.error("Exception message: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.error("Exception cause type: {}", ex.getCause().getClass().getName());
            log.error("Exception cause message: {}", ex.getCause().getMessage());
        }
        log.error("Stack trace:", ex);
        log.error("========================================");

        ErrorResponse error = ErrorResponseMapper.fromGenericException(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                exchange
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}