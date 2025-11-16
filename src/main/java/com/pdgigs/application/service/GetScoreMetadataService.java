package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.domain.validator.ScoreValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetScoreMetadataService implements GetScoreMetadataUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Mono<Score> getMetadataById(String scoreId) {
        log.debug("Retrieving metadata for score ID: {}", scoreId);

        return ScoreValidator.validateScoreId(scoreId)
                .then(scoreRepository.findById(scoreId))
                .switchIfEmpty(Mono.error(ResourceNotFoundException.score(scoreId)))
                .doOnSuccess(score -> log.info("Metadata retrieved for score: {}", scoreId));
    }
}