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
@DisplayName("Criterio 2: Subida con metadata parcial")
class UploadScoreWithPartialMetadataTest {

    @Mock
    private ScoreRepository scoreRepository;
    private UploadScoreService uploadScoreService;

    //simulando pdf valido.
    private byte[] validPdfContent;

    @BeforeEach
    void setUp() {
        uploadScoreService = new UploadScoreService(scoreRepository);

        // Crear contenido PDF válido que comience con %PDF
        String pdfHeader = "%PDF-1.4\nfake-pdf-content";
        validPdfContent = pdfHeader.getBytes();
    }

    @Test
    @DisplayName("Debe guardar partitura solo con título proporcionado")
    void uploadScore_WithOnlyTitle_ShouldSaveSuccessfully() {
        // GIVEN
        Score expectedScore = Score.builder()
                .title("Concierto Nº 5")
                .author("")
                .musicalStyle("")
                .pdfContent(validPdfContent)
                .fileSize((long) validPdfContent.length)
                .build();

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = uploadScoreService.uploadScore(validPdfContent, "Concierto Nº 5", "", "");

        // THEN
        StepVerifier.create(result)
                .expectNextMatches(score ->
                        score.getTitle().equals("Concierto Nº 5") &&
                                score.getAuthor().equals("") &&
                                score.getMusicalStyle().equals(""))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe guardar partitura solo con autor proporcionado")
    void uploadScore_WithOnlyAuthor_ShouldSaveSuccessfully() {
        // GIVEN
        Score expectedScore = Score.builder()
                .title("")
                .author("Mozart")
                .musicalStyle("")
                .pdfContent(validPdfContent)
                .fileSize((long) validPdfContent.length)
                .build();

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = uploadScoreService.uploadScore(validPdfContent, "", "Mozart", "");

        // THEN
        StepVerifier.create(result)
                .expectNextMatches(score ->
                        score.getTitle().equals("") &&
                                score.getAuthor().equals("Mozart") &&
                                score.getMusicalStyle().equals(""))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe guardar partitura solo con estilo musical proporcionado")
    void uploadScore_WithOnlyMusicalStyle_ShouldSaveSuccessfully() {
        // GIVEN
        Score expectedScore = Score.builder()
                .title("")
                .author("")
                .musicalStyle("Clásico")
                .pdfContent(validPdfContent)
                .fileSize((long) validPdfContent.length)
                .build();

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = uploadScoreService.uploadScore(validPdfContent, "", "", "Clásico");

        // THEN
        StepVerifier.create(result)
                .expectNextMatches(score ->
                        score.getTitle().equals("") &&
                                score.getAuthor().equals("") &&
                                score.getMusicalStyle().equals("Clásico"))
                .verifyComplete();
    }
}