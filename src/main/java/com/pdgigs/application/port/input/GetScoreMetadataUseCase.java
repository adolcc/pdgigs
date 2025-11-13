package com.pdgigs.application.port.input;

import com.pdgigs.domain.model.Score;
import reactor.core.publisher.Mono;

public interface GetScoreMetadataUseCase {
    Mono<Score> getMetadataById(String scoreId);
}
