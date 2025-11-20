package com.pdgigs.domain.port.output;

import com.pdgigs.domain.model.User;
import reactor.core.publisher.Mono;

public interface JwtTokenProvider {
    Mono<String> generateToken(User user);
    Mono<String> extractEmail(String token);
    Mono<Boolean> validateToken(String token);
    Mono<String> extractRole(String token);
}