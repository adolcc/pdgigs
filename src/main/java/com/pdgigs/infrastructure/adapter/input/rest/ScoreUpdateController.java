package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateScoreRequest;
import com.pdgigs.application.service.ScoreUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/scores")
@RequiredArgsConstructor
public class ScoreUpdateController {

    private final ScoreUpdateService scoreUpdateService;

    @Operation(summary = "Update score metadata by id (only musicStyle field name)")
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> updateScore(@PathVariable("id") String id,
                                                    @Valid @RequestBody UpdateScoreRequest request) {
        return scoreUpdateService.update(id, request)
                .map(updated -> ResponseEntity.ok((Object) updated))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}