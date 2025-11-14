package com.pdgigs.application.service;

import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import com.pdgigs.domain.exception.ScoreNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetScorePdfService implements GetScorePdfUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Mono<byte[]> getPdfContentById(String scoreId) {
        return scoreRepository.findById(scoreId)
                .switchIfEmpty(Mono.error(
                        new ScoreNotFoundException("Score PDF with ID " + scoreId + " not found.")
                ))
                .map(Score::pdfContent);
    }
}