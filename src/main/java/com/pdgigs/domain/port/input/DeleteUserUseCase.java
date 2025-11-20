package com.pdgigs.domain.port.input;

import reactor.core.publisher.Mono;

public interface DeleteUserUseCase {
    Mono<Void> deleteUser(String userId);
}