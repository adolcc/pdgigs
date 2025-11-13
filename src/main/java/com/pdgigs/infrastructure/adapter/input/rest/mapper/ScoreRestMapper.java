package com.pdgigs.infrastructure.adapter.input.rest.mapper;

import com.pdgigs.domain.model.Score;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class ScoreRestMapper {

    public ScoreResponse toResponse(Score score) {
        return new ScoreResponse(
                score.id(),
                score.title(),
                score.author(),
                score.musicalStyle(),
                score.fileSize()
        );
    }
}