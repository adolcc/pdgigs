package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.service.UpdateScoreRequest;
import com.pdgigs.domain.port.input.UpdateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.UploadScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UploadScoreMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreControllerUpdate {

    private final UpdateScoreUseCase updateScoreUseCase;

    @Operation(summary = "Partial update score metadata")
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UploadScoreResponse>> patchScoreMetadata(
            @PathVariable("id") String id,
            @RequestBody Mono<UpdateScoreRequest> requestMono
    ) {

        return requestMono
                .defaultIfEmpty(new UpdateScoreRequest(null, null, null))
                .flatMap(req ->
                        updateScoreUseCase.updateMetadata(id, req.getTitle(), req.getAuthor(), req.getMusicStyle())
                )
                .map(UploadScoreMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}