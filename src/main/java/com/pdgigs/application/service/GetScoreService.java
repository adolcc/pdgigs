package com.pdgigs.application.service;

import com.pdgigs.application.port.input.GetScoreMetadataUseCase;
import com.pdgigs.application.port.output.ScoreRepository;
import com.pdgigs.domain.exception.ScoreNotFoundException;
import com.pdgigs.domain.model.Score;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetScoreService implements GetScoreMetadataUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Mono<Score> getMetadataById(String scoreId) {
        return scoreRepository.findById(scoreId)
                .switchIfEmpty(Mono.error(new ScoreNotFoundException("Score not found with ID: " + scoreId)));
    }
}
