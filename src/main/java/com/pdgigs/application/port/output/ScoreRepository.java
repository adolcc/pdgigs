package com.pdgigs.application.port.output;

import com.pdgigs.domain.model.Score;
import reactor.core.publisher.Mono;

public interface ScoreRepository {
    Mono<Score> save(Score score);

    Mono<Score> findById(String id);

    Mono<Void> deleteById(String id);
}