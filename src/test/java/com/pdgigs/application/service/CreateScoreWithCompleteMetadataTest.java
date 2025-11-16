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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateScoreService - Metadata completa")
class CreateScoreWithCompleteMetadataTest {

    private static final byte[] VALID_PDF = "%PDF-1.4\nfake-pdf-content".getBytes();
    private static final String SCORE_ID = "507f1f77bcf86cd799439011";

    @Mock
    private ScoreRepository scoreRepository;

    private CreateScoreService createScoreService;

    @BeforeEach
    void setUp() {
        createScoreService = new CreateScoreService(scoreRepository);
    }

    @Test
    @DisplayName("Dado metadata completa, cuando se crea partitura, entonces guarda con todos los campos")
    void createScore_CompleteMetadata_SavesSuccessfully() {
        // GIVEN
        Score expectedScore = new Score(
                SCORE_ID,
                "Concierto Nº 5",
                "Wolfgang Amadeus Mozart",
                "Clásico",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(
                VALID_PDF,
                "Concierto Nº 5",
                "Wolfgang Amadeus Mozart",
                "Clásico"
        );

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.id()).isEqualTo(SCORE_ID);
                    assertThat(score.title()).isEqualTo("Concierto Nº 5");
                    assertThat(score.author()).isEqualTo("Wolfgang Amadeus Mozart");
                    assertThat(score.musicalStyle()).isEqualTo("Clásico");
                    assertThat(score.pdfContent()).isEqualTo(VALID_PDF);
                    assertThat(score.fileSize()).isEqualTo((long) VALID_PDF.length);
                })
                .verifyComplete();

        verify(scoreRepository).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado metadata completa con espacios, cuando se crea partitura, entonces guarda correctamente")
    void createScore_CompleteMetadataWithSpaces_SavesSuccessfully() {
        // GIVEN
        Score expectedScore = new Score(
                SCORE_ID,
                "Sinfonía No. 40 en Sol Menor",
                "Wolfgang Amadeus Mozart",
                "Música Clásica del Siglo XVIII",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(
                VALID_PDF,
                "Sinfonía No. 40 en Sol Menor",
                "Wolfgang Amadeus Mozart",
                "Música Clásica del Siglo XVIII"
        );

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.title()).contains("Sinfonía");
                    assertThat(score.author()).contains("Mozart");
                    assertThat(score.musicalStyle()).contains("Clásica");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Dado metadata completa y PDF válido, cuando se crea partitura, entonces calcula fileSize correctamente")
    void createScore_CompleteMetadata_CalculatesFileSizeCorrectly() {
        // GIVEN
        Score expectedScore = new Score(
                SCORE_ID,
                "Título",
                "Autor",
                "Estilo",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(VALID_PDF, "Título", "Autor", "Estilo");

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.fileSize()).isPositive();
                    assertThat(score.fileSize()).isEqualTo((long) VALID_PDF.length);
                })
                .verifyComplete();

        verify(scoreRepository).save(any(Score.class));
    }
}