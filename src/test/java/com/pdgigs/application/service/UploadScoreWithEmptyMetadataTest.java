package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Criterio 1: Subida con metadata vacía")
class UploadScoreWithEmptyMetadataTest {

    @Mock
    private ScoreRepository scoreRepository;
    private UploadScoreService uploadScoreService;

    private byte[] validPdfContent;

    @BeforeEach
    void setUp() {
        uploadScoreService = new UploadScoreService(scoreRepository);
        String pdfHeader = "%PDF-1.4\nfake-pdf-content";
        validPdfContent = pdfHeader.getBytes();
    }

    @Test
    @DisplayName("Debe guardar partitura exitosamente con todos los campos vacíos")
    void uploadScore_WithEmptyMetadata_ShouldSaveSuccessfully() {
        // GIVEN
        byte[] pdfContent = "fake-pdf-content".getBytes();
        Score expectedScore = new Score(
                null,
                "",
                "",
                "",
                validPdfContent,
                (long) validPdfContent.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = uploadScoreService.uploadScore(validPdfContent, "", "", "");

        // THEN
        StepVerifier.create(result)
                .expectNextMatches(score ->
                        score.title().equals("") &&
                                score.author().equals("") &&
                                score.musicalStyle().equals("") &&
                                score.pdfContent() != null)
                .verifyComplete();
    }
}