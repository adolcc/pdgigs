package com.pdgigs.domain.port.output;

import com.pdgigs.domain.model.Annotation;
import reactor.core.publisher.Mono;

public interface AnnotationRepository {
    Mono<Annotation> save(Annotation annotation);
    Mono<Annotation> findByScoreIdAndPageNumber(String scoreId, Integer pageNumber);
}