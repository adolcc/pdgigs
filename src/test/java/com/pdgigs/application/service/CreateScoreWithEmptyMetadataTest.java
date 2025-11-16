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
@DisplayName("CreateScoreService - Metadata vacía")
class CreateScoreWithEmptyMetadataTest {

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
    @DisplayName("Dado metadata vacía, cuando se crea partitura, entonces guarda con todos los campos vacíos")
    void createScore_EmptyMetadata_SavesSuccessfully() {
        // GIVEN
        Score expectedScore = new Score(
                SCORE_ID,
                "",
                "",
                "",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(VALID_PDF, "", "", "");

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.id()).isEqualTo(SCORE_ID);
                    assertThat(score.title()).isEmpty();
                    assertThat(score.author()).isEmpty();
                    assertThat(score.musicalStyle()).isEmpty();
                    assertThat(score.pdfContent()).isEqualTo(VALID_PDF);
                    assertThat(score.fileSize()).isEqualTo((long) VALID_PDF.length);
                })
                .verifyComplete();

        verify(scoreRepository).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado metadata null, cuando se crea partitura, entonces trata null como vacío")
    void createScore_NullMetadata_TreatsAsEmpty() {
        // GIVEN
        Score expectedScore = new Score(
                SCORE_ID,
                "",
                "",
                "",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(VALID_PDF, null, null, null);

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.pdfContent()).isNotNull();
                    assertThat(score.fileSize()).isEqualTo((long) VALID_PDF.length);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Dado PDF válido sin metadata, cuando se crea partitura, entonces calcula fileSize correctamente")
    void createScore_EmptyMetadata_CalculatesFileSizeCorrectly() {
        // GIVEN
        Score expectedScore = new Score(
                SCORE_ID,
                "",
                "",
                "",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(VALID_PDF, "", "", "");

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