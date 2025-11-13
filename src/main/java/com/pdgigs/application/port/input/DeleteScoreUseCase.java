package com.pdgigs.application.port.input;

import reactor.core.publisher.Mono;

public interface DeleteScoreUseCase {
    Mono<Void> deleteScore(String scoreId);
}
