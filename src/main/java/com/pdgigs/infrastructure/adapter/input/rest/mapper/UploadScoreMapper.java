package com.pdgigs.infrastructure.adapter.input.rest.mapper;

import com.pdgigs.domain.model.Score;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.UploadScoreResponse;

public final class UploadScoreMapper {

    private UploadScoreMapper() { /* util */ }

    public static UploadScoreResponse toResponse(Score score) {
        return new UploadScoreResponse(
                score.id(),
                score.title(),
                score.author(),
                score.musicStyle(),
                score.filename(),
                score.createdAt()
        );
    }
}