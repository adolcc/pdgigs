package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Criterio: Visualización de Metadata Exitosa")
class GetScoreMetadataTest {

    private static final String SCORE_ID = "507f1f77bcf86cd799439011";
    private static final String NON_EXISTENT_ID = "507f1f77bcf86cd799439099";

    @Mock
    private ScoreRepository scoreRepository;

    private GetScoreMetadataService getScoreMetadataService;

    @BeforeEach
    void setUp() {
        getScoreMetadataService = new GetScoreMetadataService(scoreRepository);
    }

    @Test
    @DisplayName("Dado que la partitura P-42 existe, cuando se solicita metadata, entonces se retorna metadata completa de forma reactiva")
    void givenScoreExists_whenMetadataIsRequested_thenFullMetadataIsReturnedReactively() {
        // GIVEN
        Score expectedScore = new Score(
                SCORE_ID,
                "Título de Prueba",
                "Autor de Prueba",
                "Estilo de Prueba",
                new byte[]{0x25, 0x50, 0x44, 0x46, 0x0A},
                5L
        );

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> resultMono = getScoreMetadataService.getMetadataById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .expectNextMatches(score ->
                        score.id().equals(SCORE_ID) &&
                                score.title().equals("Título de Prueba") &&
                                score.author().equals("Autor de Prueba") &&
                                score.musicalStyle().equals("Estilo de Prueba") &&
                                score.fileSize().equals(5L)
                )
                .verifyComplete();

        verify(scoreRepository, times(1)).findById(SCORE_ID);
    }

    @Test
    @DisplayName("Dado que la partitura tiene contenido PDF, cuando se solicita metadata, entonces el PDF está incluido")
    void givenScoreWithPdf_whenMetadataIsRequested_thenPdfContentIsIncluded() {
        // GIVEN
        byte[] pdfContent = new byte[]{0x25, 0x50, 0x44, 0x46, 0x0A};
        Score scoreWithPdf = new Score(
                SCORE_ID,
                "Sonata en Do Mayor",
                "Wolfgang Amadeus Mozart",
                "Clásico",
                pdfContent,
                (long) pdfContent.length
        );

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(scoreWithPdf));

        // WHEN
        Mono<Score> resultMono = getScoreMetadataService.getMetadataById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(score -> {
                    assertThat(score.pdfContent()).isNotNull();
                    assertThat(score.pdfContent()).hasSize(5);
                    assertThat(score.pdfContent()[0]).isEqualTo((byte) 0x25);
                })
                .verifyComplete();

        verify(scoreRepository).findById(SCORE_ID);
    }

    @Test
    @DisplayName("Fallo 404: Dado que el ID no existe, cuando se solicita metadata, entonces lanza ResourceNotFoundException")
    void givenScoreIdNotExists_whenMetadataIsRequested_thenThrowsResourceNotFoundException() {
        // GIVEN
        when(scoreRepository.findById(NON_EXISTENT_ID))
                .thenReturn(Mono.empty());

        // WHEN
        Mono<Score> resultMono = getScoreMetadataService.getMetadataById(NON_EXISTENT_ID);

        // THEN
        StepVerifier.create(resultMono)
                .expectErrorMatches(error ->
                        error instanceof ResourceNotFoundException &&
                                error.getMessage().contains("Score not found with ID: " + NON_EXISTENT_ID)
                )
                .verify();

        verify(scoreRepository).findById(NON_EXISTENT_ID);
    }

    @Test
    @DisplayName("Dado que el repositorio falla, cuando se solicita metadata, entonces propaga el error")
    void givenRepositoryFailure_whenMetadataIsRequested_thenErrorIsPropagated() {
        // GIVEN
        RuntimeException repositoryError = new RuntimeException("Database connection lost");
        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.error(repositoryError));

        // WHEN
        Mono<Score> resultMono = getScoreMetadataService.getMetadataById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Database connection lost")
                )
                .verify();

        verify(scoreRepository).findById(SCORE_ID);
    }
}