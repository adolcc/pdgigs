package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.port.input.DeleteScoreUseCase;
import com.pdgigs.application.port.input.UploadScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
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
public class ScoreController {

    private final UploadScoreUseCase uploadScoreUseCase;
    private final DeleteScoreUseCase deleteScoreUseCase;
    private final ScoreRestMapper scoreRestMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ScoreResponse> uploadScore(
            @RequestPart("file") FilePart file,
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false, defaultValue = "") String author,
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteScore(@PathVariable String id) {
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