package com.pdgigs.domain.port.input;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

public interface GetScorePdfUseCase {
    Mono<Resource> getPdf(String scoreId);
}