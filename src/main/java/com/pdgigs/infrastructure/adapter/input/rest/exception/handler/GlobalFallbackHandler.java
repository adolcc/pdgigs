package com.pdgigs.infrastructure.adapter.input.rest.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.buffer.DataBufferLimitException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
@Order(999)
@RequiredArgsConstructor
public class GlobalFallbackHandler {

    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    private boolean isDevProfile() {
        if (environment == null) return false;
        for (String profile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(profile) || "local".equalsIgnoreCase(profile)) return true;
        }
        return false;
    }

    @ExceptionHandler(DataBufferLimitException.class)
    public Mono<Void> handleDataBufferLimit(DataBufferLimitException ex, ServerWebExchange exchange) {

        log.warn("Upload failed due to DataBufferLimitException: {}", ex.getMessage(), ex);

        ServerHttpResponse response = exchange.getResponse();
        HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE;

        try {
            String path = exchange.getRequest().getPath().value();
            ErrorResponse error = new ErrorResponse(
                    "File too large or request body exceeds server limit.",
                    status.value(),
                    LocalDateTime.now(),
                    path,
                    "PAYLOAD_TOO_LARGE"
            );

            byte[] bytes = objectMapper.writeValueAsBytes(error);
            response.setStatusCode(status);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(bufferFactory.wrap(bytes)));
        } catch (Exception e) {
            log.error("Error while writing DataBufferLimitException response", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    @ExceptionHandler(Exception.class)
    public Mono<Void> handleGenericException(Exception ex, ServerWebExchange exchange) {

        log.error("❌ UNEXPECTED ERROR: {}", ex.getMessage(), ex);

        ServerHttpResponse response = exchange.getResponse();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        try {
            String path = exchange.getRequest().getPath().value();

            String message = isDevProfile() ? ex.getMessage() : "Internal server error";

            ErrorResponse error = new ErrorResponse(
                    message,
                    status.value(),
                    LocalDateTime.now(),
                    path,
                    "INTERNAL_ERROR"
            );

            byte[] bytes = objectMapper.writeValueAsBytes(error);
            response.setStatusCode(status);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return response.writeWith(Mono.just(bufferFactory.wrap(bytes)));
        } catch (Exception e) {
            log.error("Error while writing generic exception response", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }
}