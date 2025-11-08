package com.pdgigs.infrastructure.adapter.input.rest.mapper;

import com.pdgigs.domain.model.Score;
import com.pdgigs.infrastructure.adapter.input.rest.dto.ScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class ScoreMapper {

    public ScoreResponse toResponse(Score score) {
        return ScoreResponse.builder()
                .id(score.getId())
                .title(score.getTitle())
                .author(score.getAuthor())
                .musicalStyle(score.getMusicalStyle())
                .fileSize(score.getFileSize())
                .build();
    }
}