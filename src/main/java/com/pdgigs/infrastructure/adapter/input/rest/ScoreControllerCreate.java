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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "Score Upload", description = "Endpoints para subir partituras")
public class ScoreControllerCreate {

    private final CreateScoreUseCase createScoreUseCase;
    private final ScoreRestMapper scoreRestMapper;

    @Operation(
            summary = "Subir una partitura",
            description = "Permite subir un archivo PDF con metadata opcional (título, autor, estilo musical)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Partitura subida exitosamente",
                    content = @Content(schema = @Schema(implementation = ScoreResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "415", description = "Formato de archivo no válido (debe ser PDF)"),
            @ApiResponse(responseCode = "413", description = "El archivo excede el tamaño máximo permitido (10MB)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ScoreResponse> createScore(
            @Parameter(description = "Archivo PDF de la partitura", required = true)
            @RequestPart("file") FilePart file,

            @Parameter(description = "Título de la partitura")
            @RequestParam(required = false, defaultValue = "") String title,

            @Parameter(description = "Autor de la partitura")
            @RequestParam(required = false, defaultValue = "") String author,

            @Parameter(description = "Estilo musical de la partitura")
            @RequestParam(required = false, defaultValue = "") String musicalStyle
    ) {
        log.info("Uploading score - title: {}, author: {}, musicalStyle: {}", title, author, musicalStyle);

        return file.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .reduce(new byte[0], this::concatArrays)
                .flatMap(pdfContent ->
                        createScoreUseCase.createScore(pdfContent, title, author, musicalStyle))
                .map(scoreRestMapper::toResponse)
                .doOnSuccess(response -> log.info("Score uploaded successfully with ID: {}", response.id()))
                .doOnError(error -> log.error("Error uploading score", error));
    }

    private byte[] concatArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}