package com.pdgigs.application.port.input;

import reactor.core.publisher.Mono;

public interface GetScorePdfUseCase {
    Mono<byte[]> getPdfContentById(String scoreId);
}