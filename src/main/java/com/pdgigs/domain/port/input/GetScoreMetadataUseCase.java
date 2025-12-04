package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.Score;
import reactor.core.publisher.Mono;

public interface GetScoreMetadataUseCase {
    Mono<Score> findById(String scoreId);
}