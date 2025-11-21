package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.Score;
import reactor.core.publisher.Flux;

public interface AdminListScoresUseCase {
    Flux<Score> listAllScores();
}