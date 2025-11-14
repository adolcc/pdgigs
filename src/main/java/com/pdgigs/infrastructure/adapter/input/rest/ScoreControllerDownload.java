package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "Score Download", description = "Endpoints para descargar partituras")
public class ScoreControllerDownload {

    private final GetScorePdfUseCase getScorePdfUseCase;

    @Operation(
            summary = "Descargar PDF de una partitura",
            description = "Descarga el archivo PDF de una partitura por su ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF descargado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)),
            @ApiResponse(responseCode = "404", description = "Partitura no encontrada")
    })
    @GetMapping("/{id}/download")
    public Mono<ResponseEntity<byte[]>> downloadScorePdf(
            @Parameter(description = "ID de la partitura", required = true, example = "674b8e1234567890abcdef12")
            @PathVariable String id
    ) {
        log.info("Downloading PDF for score with ID: {}", id);
        return getScorePdfUseCase.getPdfContentById(id)
                .map(pdfContent -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "score-" + id + ".pdf");
                    headers.setContentLength(pdfContent.length);

                    return ResponseEntity
                            .ok()
                            .headers(headers)
                            .body(pdfContent);
                })
                .doOnSuccess(response -> log.info("PDF downloaded successfully for ID: {}", id))
                .doOnError(error -> log.error("Error downloading PDF for ID: {}", id, error));
    }
}