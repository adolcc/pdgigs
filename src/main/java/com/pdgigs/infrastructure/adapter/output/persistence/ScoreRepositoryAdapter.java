package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.infrastructure.adapter.output.persistence.entity.ScoreEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ScoreRepositoryAdapter implements ScoreRepository {

    private final MongoScoreRepository mongoScoreRepository;

    @Override
    public Mono<Score> save(Score score) {
        ScoreEntity entity = toEntity(score);
        return mongoScoreRepository.save(entity)
                .map(this::toDomain);
    }

    @Override
    public Mono<Score> findById(String id) {
        return mongoScoreRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoScoreRepository.deleteById(id);
    }

    @Override
    public Flux<Score> findAll() {
        return mongoScoreRepository.findAll()
                .map(this::toDomain);
    }

    private ScoreEntity toEntity(Score score) {
        return ScoreEntity.builder()
                .id(score.id())
                .title(score.title())
                .author(score.author())
                .musicalStyle(score.musicalStyle())
                .pdfContent(score.pdfContent())
                .fileSize(score.fileSize())
                .build();
    }

    private Score toDomain(ScoreEntity entity) {
        return new Score(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getMusicalStyle(),
                entity.getPdfContent(),
                entity.getFileSize()
        );
    }
}