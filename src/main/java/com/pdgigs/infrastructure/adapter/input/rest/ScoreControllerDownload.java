package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.dto.DownloadableScore;
import com.pdgigs.application.service.ScoreDownloadService;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(path = "/api/scores")
@RequiredArgsConstructor
public class ScoreControllerDownload {

    private final GetScoreMetadataUseCase getScoreMetadataUseCase;
    private final ScoreDownloadService scoreDownloadService;

    @Operation(summary = "Get score metadata by id")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<com.pdgigs.infrastructure.adapter.input.rest.dto.response.UploadScoreResponse> getMetadata(@PathVariable("id") String id) {
        return getScoreMetadataUseCase.findById(id)
                .map(com.pdgigs.infrastructure.adapter.input.rest.mapper.UploadScoreMapper::toResponse);
    }

    @Operation(summary = "Download score PDF by id")
    @GetMapping(path = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<Resource>> downloadPdf(@PathVariable("id") String id) {
        return scoreDownloadService.prepareDownload(id)
                .map(this::toResponseEntity)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    private ResponseEntity<Resource> toResponseEntity(DownloadableScore downloadable) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(downloadable.filename(), StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);

        downloadable.contentLength().ifPresent(headers::setContentLength);

        return ResponseEntity.ok().headers(headers).body(downloadable.resource());
    }
}