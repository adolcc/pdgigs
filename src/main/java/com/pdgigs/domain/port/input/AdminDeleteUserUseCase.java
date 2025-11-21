package com.pdgigs.domain.port.input;

import reactor.core.publisher.Mono;

public interface AdminDeleteUserUseCase {
    Mono<Void> deleteUser(String userId);
}
