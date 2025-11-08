package com.pdgigs.application.port.in;

import com.pdgigs.domain.model.Score;
import reactor.core.publisher.Mono;

public interface UploadScoreUseCase {
    Mono<Score> uploadScore(byte[] pdfContent, String title, String author, String musicalStyle);
}
