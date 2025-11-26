package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.domain.validator.ScoreValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetScorePdfService implements GetScorePdfUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Mono<byte[]> getPdfContentById(String scoreId) {
        log.debug("Retrieving PDF content for score ID: {}", scoreId);

        return ScoreValidator.validateScoreId(scoreId)
                .then(scoreRepository.findById(scoreId))
                .switchIfEmpty(Mono.defer(() -> Mono.error(ResourceNotFoundException.score(scoreId))))
                .map(Score::pdfContent)
                .doOnSuccess(pdf -> log.info("PDF content retrieved successfully for score: {}", scoreId))
                .doOnError(error -> log.error("Error retrieving PDF content for score {}: {}", scoreId, error.getMessage()));
    }
}