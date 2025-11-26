package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.Score;
import reactor.core.publisher.Mono;

public interface CreateScoreUseCase {
    Mono<Score> createScore(byte[] pdfContent, String title, String author, String musicalStyle, String userEmail);
}