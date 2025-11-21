package com.pdgigs.domain.port.input;

import reactor.core.publisher.Mono;

public interface AdminDeleteScoreUseCase {
    Mono<Void> deleteScore(String scoreId);
}