package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.infrastructure.adapter.output.persistence.entity.ScoreDocument;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ScoreRepositoryAdapter implements ScoreRepository {

    private final MongoScoreRepository mongoRepo;

    public ScoreRepositoryAdapter(MongoScoreRepository mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @Override
    public Mono<Score> save(Score score) {
        ScoreDocument doc = ScoreDocument.fromDomain(score);
        if (doc.getCreatedAt() == null) doc.setCreatedAt(java.time.LocalDateTime.now());
        return mongoRepo.save(doc).map(ScoreDocument::toDomain);
    }

    @Override
    public Mono<Score> findById(String id) {
        return mongoRepo.findById(id).map(ScoreDocument::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepo.deleteById(id);
    }

    @Override
    public Flux<Score> findAll() {
        return mongoRepo.findAll().map(ScoreDocument::toDomain);
    }
}
