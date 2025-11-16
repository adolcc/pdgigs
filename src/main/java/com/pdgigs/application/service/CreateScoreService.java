package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.CreateScoreUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.domain.validator.FileValidator;
import com.pdgigs.domain.validator.ScoreValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateScoreService implements CreateScoreUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Mono<Score> createScore(byte[] pdfContent, String title, String author, String musicalStyle) {
        log.debug("Creating new score: {} by {}", title, author);

        return FileValidator.validateFile(pdfContent)
                .then(ScoreValidator.validateMetadata(title, author, musicalStyle))
                .then(Mono.fromCallable(() -> new Score(
                        null,
                        title,
                        author,
                        musicalStyle,
                        pdfContent,
                        (long) pdfContent.length
                )))
                .flatMap(scoreRepository::save)
                .doOnSuccess(score -> log.info("Score created successfully with ID: {}", score.id()))
                .doOnError(error -> log.error("Error creating score: {}", title, error));
    }
}