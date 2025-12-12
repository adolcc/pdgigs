package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.ListScoresUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path = "/api/scores", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ScoresController {

    private final ListScoresUseCase listScoresUseCase;
    private final ScoreRestMapper scoreRestMapper;

    @GetMapping
    public Flux<ScoreResponse> listScores() {
        return listScoresUseCase.listForAuthenticatedUser()
                .map(scoreRestMapper::toResponse);
    }
}