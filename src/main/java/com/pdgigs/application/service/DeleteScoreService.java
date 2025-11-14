package com.pdgigs.application.service;

import com.pdgigs.domain.port.input.DeleteScoreUseCase;
import com.pdgigs.domain.exception.ScoreNotFoundException;
import com.pdgigs.domain.port.output.ScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteScoreService implements DeleteScoreUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Mono<Void> deleteScore(String scoreId) {
        log.info("Attempting to delete score with ID: {}", scoreId);

        return scoreRepository.findById(scoreId)
                .switchIfEmpty(Mono.error(
                        new ScoreNotFoundException("Score with ID " + scoreId + " not found.")
                ))
                .flatMap(score -> {
                    log.info("Score found. Deleting score with ID: {} and PDF content of size: {} bytes",
                            scoreId, score.pdfContent().length);
                    return scoreRepository.deleteById(scoreId);
                })
                .doOnSuccess(unused -> log.info("Score with ID {} deleted successfully", scoreId))
                .doOnError(error -> log.error("Error deleting score with ID: {}", scoreId, error));
    }
}