package com.pdgigs.domain.port.input;

import reactor.core.publisher.Mono;

public interface GetScorePdfUseCase {
    Mono<byte[]> getPdfContentById(String scoreId);
}