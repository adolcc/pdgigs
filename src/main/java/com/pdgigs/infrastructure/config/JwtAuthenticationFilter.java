package com.pdgigs.infrastructure.config;

import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.port.output.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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

        if (path.startsWith("/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars/")) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);

        if (token == null) {
            log.warn("No JWT token found in request to: {}", path);
            return chain.filter(exchange);
        }

        return jwtTokenProvider.validateToken(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        log.warn("Invalid JWT token for path: {}", path);
                        return chain.filter(exchange);
                    }

                    return jwtTokenProvider.extractEmail(token)
                            .flatMap(userRepository::findByEmail)
                            .flatMap(user -> {
                                List<SimpleGrantedAuthority> authorities =
                                        List.of(new SimpleGrantedAuthority(user.role()));

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                user.email(),
                                                null,
                                                authorities
                                        );

                                log.info("Authenticated user: {} with role: {}", user.email(), user.role());

                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                            });
                })
                .onErrorResume(error -> {
                    log.error("Error processing JWT token: {}", error.getMessage());
                    return chain.filter(exchange);
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