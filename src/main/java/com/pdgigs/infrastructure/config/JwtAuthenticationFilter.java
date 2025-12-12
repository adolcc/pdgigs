package com.pdgigs.infrastructure.config;

import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.port.output.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.debug("Allowing OPTIONS request for path: {}", path);
            return chain.filter(exchange);
        }

        if (path.startsWith("/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars/")) {
            log.debug("Allowing public access to path: {}", path);
            return chain.filter(exchange);
        }

        String token = extractToken(request);

        if (token == null) {
            log.debug("No JWT token found in request to: {}", path);
            return chain.filter(exchange);
        }

        log.debug("JWT token present, validating for path: {}", path);

        return jwtTokenProvider.validateToken(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        log.warn("Invalid JWT token for path: {}", path);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    return jwtTokenProvider.extractEmail(token)
                            .flatMap(userRepository::findByEmail)
                            .flatMap(user -> {
                                if (user == null) {
                                    log.warn("User not found for token email on path: {}", path);
                                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                    return exchange.getResponse().setComplete();
                                }

                                List<SimpleGrantedAuthority> authorities =
                                        List.of(new SimpleGrantedAuthority(user.role()));

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                user.email(),
                                                null,
                                                authorities
                                        );

                                log.debug("Authenticated user: {} with role: {} for path: {}", user.email(), user.role(), path);

                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                            });
                })
                .onErrorResume(error -> {
                    log.error("Error processing JWT token for path {}: {}", path, error.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}