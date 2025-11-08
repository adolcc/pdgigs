package com.pdgigs.infrastructure.adapter.input.rest.mapper;

import com.pdgigs.domain.model.Score;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class ScoreRestMapper {

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