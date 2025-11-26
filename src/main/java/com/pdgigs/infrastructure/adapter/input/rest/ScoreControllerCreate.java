package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.CreateScoreUseCase;
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
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Slf4j
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "Score Upload", description = "Endpoint to upload scores")
public class ScoreControllerCreate {

    private final CreateScoreUseCase createScoreUseCase;
    private final ScoreRestMapper scoreRestMapper;

    @Operation(
            summary = "Upload score",
            description = "Allows you to upload a PDF file with optional metadata (title, author, musical style)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sheet music successfully uploaded",
                    content = @Content(schema = @Schema(implementation = ScoreResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "415", description = "Invalid file format (must be PDF)"),
            @ApiResponse(responseCode = "413", description = "The file exceeds the maximum allowed size (10MB)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ScoreResponse> createScore(
            @Parameter(description = "PDF file of the score", required = true)
            @RequestPart("file") FilePart file,

            @Parameter(description = "Title of the score")
            @RequestPart(name = "title", required = false) String title,

            @Parameter(description = "Author of the score")
            @RequestPart(name = "author", required = false) String author,

            @Parameter(description = "Musical style of the score")
            @RequestPart(name = "musicalStyle", required = false) String musicalStyle,

            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        log.info("Uploading score - title: {}, author: {}, musicalStyle: {}, user: {}",
                title, author, musicalStyle, userEmail);

        return DataBufferUtils.join(file.content())
                .map(dataBuffer -> {
                    try {
                        ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes);
                        return bytes;
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .flatMap(pdfContent ->
                        createScoreUseCase.createScore(
                                pdfContent,
                                title == null ? "" : title,
                                author == null ? "" : author,
                                musicalStyle == null ? "" : musicalStyle,
                                userEmail
                        ))
                .map(scoreRestMapper::toResponse)
                .doOnSuccess(response -> log.info("Score uploaded successfully with ID: {} for user: {}",
                        response.id(), userEmail))
                .doOnError(error -> log.error("Error uploading score for user: {}", userEmail, error));
    }
}