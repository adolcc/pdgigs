package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.Score;
import reactor.core.publisher.Flux;

public interface ListScoresUseCase {
    Flux<Score> listForAuthenticatedUser();
}