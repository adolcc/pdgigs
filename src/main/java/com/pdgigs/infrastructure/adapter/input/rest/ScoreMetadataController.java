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

    // Si en el futuro quieres listar todas las partituras, añade findAll() a GetScoreMetadataUseCase
    // y descomenta / implementa este endpoint:
    //
    // @GetMapping
    // public Flux<ScoreResponse> getAllScores() {
    //     return getScoreMetadataUseCase.findAll()
    //             .map(scoreRestMapper::toResponse);
    // }
}