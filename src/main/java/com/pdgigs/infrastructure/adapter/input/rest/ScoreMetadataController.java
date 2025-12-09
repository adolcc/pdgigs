package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/scores")
@RequiredArgsConstructor
public class ScoreMetadataController {

    private final GetScoreMetadataUseCase getScoreMetadataUseCase;
    private final ScoreRestMapper scoreRestMapper;

    @GetMapping("/{scoreId}")
    public Mono<ScoreResponse> getScoreMetadata(@PathVariable("scoreId") String scoreId) {
        return getScoreMetadataUseCase.findById(scoreId)
                .map(scoreRestMapper::toResponse);
    }

    // @GetMapping
    // public Flux<ScoreResponse> getAllScores() {
    //     return getScoreMetadataUseCase.findAll()
    //             .map(scoreRestMapper::toResponse);
    // }
}