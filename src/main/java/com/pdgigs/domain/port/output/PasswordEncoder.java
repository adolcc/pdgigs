package com.pdgigs.domain.port.output;

import reactor.core.publisher.Mono;

public interface PasswordEncoder {
    Mono<String> encode(String rawPassword);
    Mono<Boolean> matches(String rawPassword, String encodedPassword);
}