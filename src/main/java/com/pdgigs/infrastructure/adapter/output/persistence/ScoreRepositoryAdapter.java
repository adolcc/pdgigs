package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.infrastructure.adapter.output.persistence.entity.ScoreEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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

    private ScoreEntity toEntity(Score score) {
        return ScoreEntity.builder()
                .id(score.getId())
                .title(score.getTitle())
                .author(score.getAuthor())
                .musicalStyle(score.getMusicalStyle())
                .pdfContent(score.getPdfContent())
                .fileSize(score.getFileSize())
                .build();
    }

    private Score toDomain(ScoreEntity entity) {
        return Score.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .musicalStyle(entity.getMusicalStyle())
                .pdfContent(entity.getPdfContent())
                .fileSize(entity.getFileSize())
                .build();
    }
}