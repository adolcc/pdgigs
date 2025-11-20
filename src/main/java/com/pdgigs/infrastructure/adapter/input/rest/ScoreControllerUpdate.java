package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.UpdateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateScoreRequest;
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
@Tag(name = "Score Update", description = "Endpoint to update scores")
public class ScoreControllerUpdate {

    private final UpdateScoreUseCase updateScoreUseCase;
    private final ScoreRestMapper scoreRestMapper;

    @Operation(
            summary = "Update metadata of a score",
            description = "Partially or fully updates the metadata of a score (title, author, musical style)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata successfully updated",
                    content = @Content(schema = @Schema(implementation = ScoreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Score not found")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ScoreResponse> updateScoreMetadata(
            @Parameter(description = "Score ID", required = true, example = "674b8e1234567890abcdef12")
            @PathVariable String id,

            @RequestBody UpdateScoreRequest request
    ) {
        log.info("Updating metadata for score with ID: {} - Request: {}", id, request);
        return updateScoreUseCase.updateMetadata(id, request.title(), request.author(), request.musicalStyle())
                .map(scoreRestMapper::toResponse)
                .doOnSuccess(response -> log.info("Metadata updated successfully for ID: {}", id))
                .doOnError(error -> log.error("Error updating metadata for ID: {}", id, error));
    }
}