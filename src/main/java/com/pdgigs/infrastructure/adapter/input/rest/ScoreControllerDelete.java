package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.DeleteScoreUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Score Delete", description = "Endpoints para eliminar partituras")
public class ScoreControllerDelete {

    private final DeleteScoreUseCase deleteScoreUseCase;

    @Operation(
            summary = "Eliminar una partitura",
            description = "Elimina una partitura y su archivo PDF asociado usando su ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Partitura eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Partitura no encontrada")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteScore(
            @Parameter(description = "ID de la partitura a eliminar", required = true, example = "P-55")
            @PathVariable String id
    ) {
        log.info("Received request to delete score with ID: {}", id);
        return deleteScoreUseCase.deleteScore(id)
                .doOnSuccess(unused -> log.info("Score with ID {} deleted successfully", id))
                .doOnError(error -> log.error("Error deleting score with ID: {}", id, error));
    }
}