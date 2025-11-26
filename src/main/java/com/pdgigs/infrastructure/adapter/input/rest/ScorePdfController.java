package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.service.ScoreSecurityService;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/scores/pdf")
@RequiredArgsConstructor
public class ScorePdfController {

    private final GetScorePdfUseCase getScorePdfUseCase;
    private final ScoreSecurityService scoreSecurityService;

    @GetMapping("/{scoreId}")
    public Mono<ResponseEntity<byte[]>> getScorePdf(@PathVariable String scoreId,
                                                    Authentication authentication) {
        String currentUserEmail = authentication.getName();

        return scoreSecurityService.hasAccessToScore(scoreId, currentUserEmail)
                .flatMap(hasAccess -> {
                    if (!hasAccess) {
                        return Mono.just(ResponseEntity.status(403).build());
                    }

                    return getScorePdfUseCase.getPdfContentById(scoreId)
                            .map(pdfContent -> ResponseEntity.ok()
                                    .contentType(MediaType.APPLICATION_PDF)
                                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"score-" + scoreId + ".pdf\"")
                                    .body(pdfContent))
                            .defaultIfEmpty(ResponseEntity.notFound().build());
                });
    }
}