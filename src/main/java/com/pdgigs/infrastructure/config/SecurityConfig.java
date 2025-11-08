package com.pdgigs.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)  // Deshabilitar CSRF para APIs REST
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/**").permitAll()  // Permitir acceso a /api/**
                        .anyExchange().authenticated()         // Requerir autenticación para el resto
                )
                .build();
    }
}