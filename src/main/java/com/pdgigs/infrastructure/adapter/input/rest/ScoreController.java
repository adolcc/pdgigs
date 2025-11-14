package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.DeleteScoreUseCase;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import com.pdgigs.domain.port.input.UpdateScoreUseCase;
import com.pdgigs.domain.port.input.UploadScoreUseCase;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "Scores", description = "API para gestión de partituras musicales")
public class ScoreController {

    private final UploadScoreUseCase uploadScoreUseCase;
    private final DeleteScoreUseCase deleteScoreUseCase;
    private final GetScoreMetadataUseCase getScoreMetadataUseCase;
    private final GetScorePdfUseCase getScorePdfUseCase;
    private final UpdateScoreUseCase updateScoreUseCase;
    private final ScoreRestMapper scoreRestMapper;

    @Operation(
            summary = "Subir una partitura",
            description = "Permite subir un archivo PDF con metadata opcional (título, autor, estilo musical)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Partitura subida exitosamente",
                    content = @Content(schema = @Schema(implementation = ScoreResponse.class))),
            @ApiResponse(responseCode = "415", description = "Formato de archivo no válido (debe ser PDF)"),
            @ApiResponse(responseCode = "413", description = "El archivo excede el tamaño máximo permitido (10MB)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ScoreResponse> uploadScore(
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
                        uploadScoreUseCase.uploadScore(pdfContent, title, author, musicalStyle))
                .map(scoreRestMapper::toResponse)
                .doOnSuccess(response -> log.info("Score uploaded successfully with ID: {}", response.id()))
                .doOnError(error -> log.error("Error uploading score", error));
    }

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

    @Operation(
            summary = "Actualizar metadata de una partitura",
            description = "Actualiza parcial o totalmente la metadata de una partitura (título, autor, estilo musical)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = ScoreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Partitura no encontrada")
    })
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ScoreResponse> updateScoreMetadata(
            @Parameter(description = "ID de la partitura", required = true, example = "674b8e1234567890abcdef12")
            @PathVariable String id,

            @RequestBody UpdateScoreRequest request
    ) {
        log.info("Updating metadata for score with ID: {} - Request: {}", id, request);
        return updateScoreUseCase.updateMetadata(id, request.title(), request.author(), request.musicalStyle())
                .map(scoreRestMapper::toResponse)
                .doOnSuccess(response -> log.info("Metadata updated successfully for ID: {}", id))
                .doOnError(error -> log.error("Error updating metadata for ID: {}", id, error));
    }

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

    private byte[] concatArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}