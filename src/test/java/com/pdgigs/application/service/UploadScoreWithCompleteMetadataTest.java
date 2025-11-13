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
@DisplayName("Criterio 3: Subida con metadata completa")
class UploadScoreWithCompleteMetadataTest {

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
    @DisplayName("Debe guardar partitura con todos los campos proporcionados")
    void uploadScore_WithCompleteMetadata_ShouldSaveSuccessfully() {
        // GIVEN
        byte[] pdfContent = "fake-pdf-content".getBytes();
        Score expectedScore = new Score(
                null,
                "Concierto Nº 5",
                "Mozart",
                "Clásico",
                validPdfContent,
                (long) validPdfContent.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = uploadScoreService.uploadScore(validPdfContent, "Concierto Nº 5", "Mozart", "Clásico");

        // THEN
        StepVerifier.create(result)
                .expectNextMatches(score ->
                        score.title().equals("Concierto Nº 5") &&
                                score.author().equals("Mozart") &&
                                score.musicalStyle().equals("Clásico") &&
                                score.pdfContent() != null)
                .verifyComplete();
    }
}