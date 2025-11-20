package com.pdgigs.domain.port.input;

import reactor.core.publisher.Mono;

public interface AuthenticateUserUseCase {
    Mono<String> authenticate(String email, String password);
}
