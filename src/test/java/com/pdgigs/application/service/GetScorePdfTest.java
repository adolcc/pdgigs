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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetScorePdfService - Descarga de PDF")
class GetScorePdfTest {

    private static final String SCORE_ID = "507f1f77bcf86cd799439011";
    private static final String NON_EXISTENT_ID = "507f1f77bcf86cd799439099";
    private static final byte[] VALID_PDF = {0x25, 0x50, 0x44, 0x46, 0x0A, 0x01, 0x02};

    @Mock
    private ScoreRepository scoreRepository;

    private GetScorePdfService getScorePdfService;

    @BeforeEach
    void setUp() {
        getScorePdfService = new GetScorePdfService(scoreRepository);
    }

    @Test
    @DisplayName("Dado que existe partitura, cuando se solicita PDF, entonces retorna contenido binario")
    void getPdf_ScoreExists_ReturnsPdfContent() {
        // GIVEN
        Score existingScore = new Score(
                SCORE_ID,
                "Sonata en Do Mayor",
                "Mozart",
                "Clásico",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));

        // WHEN
        Mono<byte[]> resultMono = getScorePdfService.getPdfContentById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(pdfContent -> {
                    assertThat(pdfContent).isEqualTo(VALID_PDF);
                    assertThat(pdfContent).hasSize(VALID_PDF.length);
                    assertThat(pdfContent[0]).isEqualTo((byte) 0x25);
                    assertThat(pdfContent[1]).isEqualTo((byte) 0x50);
                })
                .verifyComplete();

        verify(scoreRepository).findById(SCORE_ID);
    }

    @Test
    @DisplayName("Dado que no existe partitura, cuando se solicita PDF, entonces lanza ResourceNotFoundException")
    void getPdf_ScoreNotFound_ThrowsResourceNotFoundException() {
        // GIVEN
        when(scoreRepository.findById(NON_EXISTENT_ID))
                .thenReturn(Mono.empty());

        // WHEN
        Mono<byte[]> resultMono = getScorePdfService.getPdfContentById(NON_EXISTENT_ID);

        // THEN
        StepVerifier.create(resultMono)
                .expectErrorMatches(error ->
                        error instanceof ResourceNotFoundException &&
                                error.getMessage().contains("Score not found with ID: " + NON_EXISTENT_ID))
                .verify();

        verify(scoreRepository).findById(NON_EXISTENT_ID);
    }

    @Test
    @DisplayName("Dado que existe partitura sin contenido, cuando se solicita PDF, entonces retorna array vacío")
    void getPdf_EmptyPdfContent_ReturnsEmptyArray() {
        // GIVEN
        byte[] emptyPdf = new byte[0];
        Score scoreWithoutContent = new Score(
                SCORE_ID,
                "Sin Contenido",
                "Desconocido",
                "N/A",
                emptyPdf,
                0L
        );

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(scoreWithoutContent));

        // WHEN
        Mono<byte[]> resultMono = getScorePdfService.getPdfContentById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(pdfContent -> {
                    assertThat(pdfContent).isEmpty();
                    assertThat(pdfContent).hasSize(0);
                })
                .verifyComplete();

        verify(scoreRepository).findById(SCORE_ID);
    }

    @Test
    @DisplayName("Dado PDF grande, cuando se solicita, entonces retorna contenido completo")
    void getPdf_LargePdf_ReturnsFullContent() {
        // GIVEN
        byte[] largePdf = new byte[10 * 1024 * 1024];
        largePdf[0] = 0x25;
        largePdf[1] = 0x50;
        largePdf[2] = 0x44;
        largePdf[3] = 0x46;

        Score scoreWithLargePdf = new Score(
                SCORE_ID,
                "Partitura Grande",
                "Autor",
                "Estilo",
                largePdf,
                (long) largePdf.length
        );

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(scoreWithLargePdf));

        // WHEN
        Mono<byte[]> resultMono = getScorePdfService.getPdfContentById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(pdfContent -> {
                    assertThat(pdfContent).hasSize(10 * 1024 * 1024);
                    assertThat(pdfContent[0]).isEqualTo((byte) 0x25);
                })
                .verifyComplete();
    }
}