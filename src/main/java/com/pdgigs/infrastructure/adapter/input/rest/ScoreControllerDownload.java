package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "Score Download", description = "Endpoint to download scores")
@ConditionalOnProperty(name = "features.download-score.enabled", havingValue = "true", matchIfMissing = false)
public class ScoreControllerDownload {

    private final GetScorePdfUseCase getScorePdfUseCase;

    @Operation(summary = "Download PDF of sheet music", description = "Download the PDF file of a score using its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Score not found")
    })
    @GetMapping(path = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<Resource>> downloadScorePdf(
            @Parameter(description = "Score ID", required = true, example = "P-42")
            @PathVariable String id
    ) {
        log.info("Downloading PDF for score with ID: {}", id);

        return getScorePdfUseCase.getPdf(id)
                .map(resource -> {
                    ContentDisposition contentDisposition = ContentDisposition.attachment()
                            .filename("score-" + id + ".pdf")
                            .build();

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDisposition(contentDisposition);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(resource);
                })
                .doOnSuccess(r -> log.info("PDF prepared for ID: {}", id))
                .doOnError(err -> log.error("Error preparing PDF for ID: {}", id, err));
    }
}