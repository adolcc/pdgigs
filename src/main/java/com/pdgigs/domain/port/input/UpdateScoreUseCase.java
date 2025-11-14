package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.Score;
import reactor.core.publisher.Mono;

public interface UpdateScoreUseCase{
    Mono<Score> updateMetadata(String id, String title, String author, String musicalStyle);
}