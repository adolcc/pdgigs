package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.DeleteScoreUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreControllerDelete {

    private final DeleteScoreUseCase deleteScoreUseCase;

    @Operation(summary = "Delete a score by id")
    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<Void>> deleteScore(@PathVariable("id") String id) {
        return deleteScoreUseCase.deleteById(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}