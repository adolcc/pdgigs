package com.pdgigs.application.service;

import com.pdgigs.application.port.output.ScoreRepository;
import com.pdgigs.domain.exception.ScoreNotFoundException;
import com.pdgigs.domain.model.Score;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@ExtendWith(MockitoExtension.class)
@DisplayName("Criterio: Lectura del Archivo PDF Exitosa")
class GetScorePdfTest {

    private static final String SCORE_ID = "P-42";
    private static final byte[] EXPECTED_PDF_CONTENT = new byte[]{0x25, 0x50, 0x44, 0x46, 0x0A, 0x01, 0x02};

    @Mock
    private ScoreRepository scoreRepository;

    private GetScorePdfService getScorePdfService;

    @BeforeEach
    void setUp() {
        getScorePdfService = new GetScorePdfService(scoreRepository);
    }

    @Test
    @DisplayName("Criterio: Descarga de Archivo PDF")
    void givenScoreExists_whenPdfIsRequested_thenBinaryContentIsServed() {

        Score existingScore = new Score(
                SCORE_ID,
                "Título",
                "Autor",
                "Estilo",
                EXPECTED_PDF_CONTENT,
                (long) EXPECTED_PDF_CONTENT.length
        );

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));

        // WHEN
        Mono<byte[]> resultMono = getScorePdfService.getPdfContentById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .expectNextMatches(pdfContent -> {
                    assertArrayEquals(EXPECTED_PDF_CONTENT, pdfContent);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Descarga Exitosa de Contenido PDF")
    void givenScoreNotExists_whenPdfIsRequested_thenThrowsScoreNotFoundException() {

        // GIVEN
        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.empty());

        // WHEN
        Mono<byte[]> resultMono = getScorePdfService.getPdfContentById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .expectError(ScoreNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Devuelve array vacío si el PDF existe pero no tiene contenido (longitud cero)")
    void givenScoreExists_whenPdfContentIsEmpty_thenReturnsEmptyByteArray() {

        // GIVEN
        final byte[] emptyPdfContent = new byte[0];
        Score existingScoreWithoutContent = new Score(
                SCORE_ID,
                "Título",
                "Autor",
                "Estilo",
                emptyPdfContent,
                0L
        );

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScoreWithoutContent));

        // WHEN
        Mono<byte[]> resultMono = getScorePdfService.getPdfContentById(SCORE_ID);

        // THEN
        StepVerifier.create(resultMono)
                .expectNextMatches(pdfContent -> {
                    assertArrayEquals(emptyPdfContent, pdfContent);
                    return pdfContent.length == 0;
                })
                .verifyComplete();
    }
}