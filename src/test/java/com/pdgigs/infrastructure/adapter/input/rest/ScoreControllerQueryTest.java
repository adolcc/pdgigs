package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "Score Query", description = "Endpoints para consultar información de partituras")
public class ScoreControllerQueryTest {

    private final GetScoreMetadataUseCase getScoreMetadataUseCase;
    private final ScoreRestMapper scoreRestMapper;

    @Operation(
            summary = "Obtener metadata de una partitura",
            description = "Obtiene la información (metadata) de una partitura por su ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ScoreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Partitura no encontrada")
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ScoreResponse> getScoreMetadata(
            @Parameter(description = "ID de la partitura", required = true, example = "674b8e1234567890abcdef12")
            @PathVariable String id
    ) {
        log.info("Getting metadata for score with ID: {}", id);
        return getScoreMetadataUseCase.getMetadataById(id)
                .map(scoreRestMapper::toResponse)
                .doOnSuccess(response -> log.info("Metadata retrieved successfully for ID: {}", id))
                .doOnError(error -> log.error("Error getting metadata for ID: {}", id, error));
    }
}