package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.service.AnnotationService;
import com.pdgigs.domain.port.output.StorageRepository;
import com.pdgigs.infrastructure.adapter.output.persistence.entity.AnnotationDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/scores/{scoreId}/annotations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class AnnotationController {

    private final StorageRepository storageRepository;
    private final AnnotationService annotationService;

    public static record AnnotationDto(Integer pageNumber, String annotationsJson) {}

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AnnotationDocument>> save(@PathVariable String scoreId, @RequestBody AnnotationDto dto) {
        return annotationService.saveAnnotations(scoreId, dto.pageNumber(), dto.annotationsJson())
                .map(saved -> ResponseEntity.ok(AnnotationDocument.fromDomain(saved)))
                .onErrorResume(err -> {
                    log.error("Error saving annotations: {}", err.getMessage(), err);
                    return Mono.just(ResponseEntity.status(500).build());
                });
    }

    /**
     * GET returns 200 with an AnnotationDocument:
     * - If an annotation exists for (scoreId,page) -> 200 with the document
     * - If not exists -> 200 with an "empty" AnnotationDocument (annotationsJson = "")
     *
     * Esto evita que el frontend tenga que manejar 404 por "no hay anotaciones".
     */
    @GetMapping
    public Mono<ResponseEntity<AnnotationDocument>> load(@PathVariable String scoreId, @RequestParam("page") Integer page) {
        return annotationService.loadAnnotations(scoreId, page)
                .map(a -> ResponseEntity.ok(AnnotationDocument.fromDomain(a)))
                .defaultIfEmpty(ResponseEntity.ok(makeEmptyDocument(scoreId, page)));
    }

    private AnnotationDocument makeEmptyDocument(String scoreId, Integer page) {
        AnnotationDocument d = new AnnotationDocument();
        d.setId(null);
        d.setScoreId(scoreId);
        d.setPageNumber(page);
        d.setAnnotationsJson("");
        d.setUpdatedAt(null);
        d.setUpdatedBy("");
        return d;
    }

    @GetMapping("/upload-url")
    public Mono<ResponseEntity<Map<String, String>>> getUploadUrl(@PathVariable String scoreId) {
        // Definimos la ruta donde se guardará en S3
        String fileName = "scores/" + scoreId + "/annotated-" + System.currentTimeMillis() + ".pdf";

        return storageRepository.generatePresignedUploadUrl(fileName)
                .map(url -> ResponseEntity.ok(Map.of(
                        "uploadUrl", url,
                        "fileName", fileName
                )));
    }
}