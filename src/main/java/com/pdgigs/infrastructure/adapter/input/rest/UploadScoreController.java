package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.UploadScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.UploadScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UploadScoreMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping(path = "/api/scores", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class UploadScoreController {

    private final UploadScoreUseCase uploadScoreUseCase;

    @Operation(summary = "Fast upload a score (empty metadata allowed)")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<UploadScoreResponse>> fastUpload(
            @RequestPart("file") FilePart file,
            @RequestPart(value = "title", required = false) String title,
            @RequestPart(value = "author", required = false) String author,
            @RequestPart(value = "musicStyle", required = false) String musicStyle,
            @RequestParam(value = "userId", required = false) String userId
    ) {
        return uploadScoreUseCase.upload(file, title, author, musicStyle, userId)
                .map(UploadScoreMapper::toResponse)
                .map(response -> {
                    URI location = URI.create("/api/scores/" + response.id());
                    return ResponseEntity.created(location).body(response);
                });
    }
}