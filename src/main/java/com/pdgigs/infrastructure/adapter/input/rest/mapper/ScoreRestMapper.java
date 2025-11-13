package com.pdgigs.infrastructure.adapter.input.rest.mapper;

import com.pdgigs.domain.model.Score;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class ScoreRestMapper {

    public ScoreResponse toResponse(Score score) {
        return ScoreResponse.builder()
                .id(score.id())
                .title(score.title())
                .author(score.author())
                .musicalStyle(score.musicalStyle())
                .fileSize(score.fileSize())
                .build();
    }
}