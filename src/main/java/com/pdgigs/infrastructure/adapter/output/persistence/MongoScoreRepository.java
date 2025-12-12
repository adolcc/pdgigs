package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.infrastructure.adapter.output.persistence.entity.ScoreDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoScoreRepository extends ReactiveMongoRepository<ScoreDocument, String> {
}