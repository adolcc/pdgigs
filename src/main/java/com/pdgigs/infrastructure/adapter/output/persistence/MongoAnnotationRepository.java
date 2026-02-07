package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.infrastructure.adapter.output.persistence.entity.AnnotationDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoAnnotationRepository extends ReactiveMongoRepository<AnnotationDocument, String> {
    Mono<AnnotationDocument> findByScoreIdAndPageNumber(String scoreId, Integer pageNumber);
}