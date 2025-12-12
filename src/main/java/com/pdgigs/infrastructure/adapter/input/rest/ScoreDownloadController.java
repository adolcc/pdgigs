package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.service.ScoreDownloadService;
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
public class ScoreDownloadController {

    private final ScoreDownloadService scoreDownloadService;

    @Operation(summary = "Download score PDF by id")
    @GetMapping("/{id}/pdf")
    public Mono<ResponseEntity<Resource>> downloadPdf(@PathVariable("id") String id) {
        return scoreDownloadService.prepareDownload(id)
                .map(downloadable -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);

                    ContentDisposition contentDisposition = ContentDisposition.attachment()
                            .filename(downloadable.filename(), StandardCharsets.UTF_8)
                            .build();
                    headers.setContentDisposition(contentDisposition);

                    downloadable.contentLength().ifPresent(headers::setContentLength);

                    return ResponseEntity.ok().headers(headers).body(downloadable.resource());
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}