package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.infrastructure.adapter.input.rest.dto.response.UploadScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UploadScoreMapper;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/scores")
@RequiredArgsConstructor
public class ScoreControllerRead {

    private final GetScoreMetadataUseCase getScoreMetadataUseCase;
    private final GetScorePdfUseCase getScorePdfUseCase;

    @Operation(summary = "Get score metadata by id")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UploadScoreResponse> getMetadata(@PathVariable("id") String id) {
        return getScoreMetadataUseCase.findById(id)
                .map(UploadScoreMapper::toResponse);
    }

    @Operation(summary = "Download score PDF by id")
    @GetMapping(path = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<Resource>> downloadPdf(@PathVariable("id") String id) {
        return getScorePdfUseCase.getPdf(id)
                .flatMap(resource ->
                        Mono.fromCallable(resource::contentLength)
                                .map(length -> ResponseEntity.ok()
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .contentLength(length >= 0 ? length : -1)
                                        .body(resource))
                                .onErrorResume(e -> Mono.just(ResponseEntity.ok()
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .body(resource)))
                );
    }
}