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
@DisplayName("CreateScoreService - Metadata parcial")
class CreateScoreWithPartialMetadataTest {

    private static final byte[] VALID_PDF = "%PDF-1.4\nfake-pdf-content".getBytes();

    @Mock
    private ScoreRepository scoreRepository;

    private CreateScoreService createScoreService;

    @BeforeEach
    void setUp() {
        createScoreService = new CreateScoreService(scoreRepository);
    }

    @Test
    @DisplayName("Dado solo título, cuando se crea partitura, entonces guarda con autor y estilo vacíos")
    void createScore_OnlyTitle_SavesWithEmptyAuthorAndStyle() {
        // GIVEN
        String title = "Concierto Nº 5";
        Score expectedScore = new Score(
                "507f1f77bcf86cd799439011",
                title,
                "",
                "",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(VALID_PDF, title, "", "");

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.title()).isEqualTo(title);
                    assertThat(score.author()).isEmpty();
                    assertThat(score.musicalStyle()).isEmpty();
                    assertThat(score.pdfContent()).isEqualTo(VALID_PDF);
                })
                .verifyComplete();

        verify(scoreRepository).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado solo autor, cuando se crea partitura, entonces guarda con título y estilo vacíos")
    void createScore_OnlyAuthor_SavesWithEmptyTitleAndStyle() {
        // GIVEN
        String author = "Wolfgang Amadeus Mozart";
        Score expectedScore = new Score(
                "507f1f77bcf86cd799439012",
                "",
                author,
                "",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(VALID_PDF, "", author, "");

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.title()).isEmpty();
                    assertThat(score.author()).isEqualTo(author);
                    assertThat(score.musicalStyle()).isEmpty();
                })
                .verifyComplete();

        verify(scoreRepository).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado solo estilo musical, cuando se crea partitura, entonces guarda con título y autor vacíos")
    void createScore_OnlyMusicalStyle_SavesWithEmptyTitleAndAuthor() {
        // GIVEN
        String musicalStyle = "Clásico";
        Score expectedScore = new Score(
                "507f1f77bcf86cd799439013",
                "",
                "",
                musicalStyle,
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(VALID_PDF, "", "", musicalStyle);

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.title()).isEmpty();
                    assertThat(score.author()).isEmpty();
                    assertThat(score.musicalStyle()).isEqualTo(musicalStyle);
                })
                .verifyComplete();

        verify(scoreRepository).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado título y autor, cuando se crea partitura, entonces guarda con estilo vacío")
    void createScore_TitleAndAuthor_SavesWithEmptyStyle() {
        // GIVEN
        String title = "Sinfonía No. 40";
        String author = "Mozart";
        Score expectedScore = new Score(
                "507f1f77bcf86cd799439014",
                title,
                author,
                "",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.just(expectedScore));

        // WHEN
        Mono<Score> result = createScoreService.createScore(VALID_PDF, title, author, "");

        // THEN
        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.title()).isEqualTo(title);
                    assertThat(score.author()).isEqualTo(author);
                    assertThat(score.musicalStyle()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Dado metadata parcial con valores null, cuando se crea partitura, entonces trata null como vacío")
    void createScore_NullMetadata_TreatsAsEmpty() {
        // GIVEN
        Score expectedScore = new Score(
                "507f1f77bcf86cd799439015",
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
                    assertThat(score.pdfContent()).isEqualTo(VALID_PDF);
                    assertThat(score.fileSize()).isEqualTo((long) VALID_PDF.length);
                })
                .verifyComplete();
    }
}