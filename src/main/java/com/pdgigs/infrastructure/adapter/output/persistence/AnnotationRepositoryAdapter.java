package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.domain.model.Annotation;
import com.pdgigs.domain.port.output.AnnotationRepository;
import com.pdgigs.infrastructure.adapter.output.persistence.entity.AnnotationDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class AnnotationRepositoryAdapter implements AnnotationRepository {

    private final MongoAnnotationRepository mongoRepository;

    @Override
    public Mono<Annotation> save(Annotation annotation) {
        return mongoRepository.findByScoreIdAndPageNumber(annotation.scoreId(), annotation.pageNumber())
                .flatMap(existing -> {
                    existing.setAnnotationsJson(annotation.annotationsJson());
                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                    existing.setUpdatedBy(annotation.updatedBy());
                    return mongoRepository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    AnnotationDocument doc = AnnotationDocument.fromDomain(annotation);
                    if (doc.getUpdatedAt() == null) doc.setUpdatedAt(java.time.LocalDateTime.now());
                    return mongoRepository.save(doc);
                }))
                .map(AnnotationDocument::toDomain);
    }

    @Override
    public Mono<Annotation> findByScoreIdAndPageNumber(String scoreId, Integer pageNumber) {
        return mongoRepository.findByScoreIdAndPageNumber(scoreId, pageNumber).map(AnnotationDocument::toDomain);
    }
}