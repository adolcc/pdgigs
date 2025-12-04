package com.pdgigs.domain.port.input;

import reactor.core.publisher.Mono;

public interface DeleteScoreUseCase {
    Mono<Void> deleteById(String id);
}
