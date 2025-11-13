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

@ExtendWith(MockitoExtension.class)
@DisplayName("Criterio: Visualización de Metadata Exitosa")
class GetScoreMetadataTest {

    private static final String SCORE_ID = "P-42";

    @Mock
    private ScoreRepository scoreRepository;

    private GetScoreService getScoreService;

    @BeforeEach
    void setUp() {
        getScoreService = new GetScoreService(scoreRepository);
    }

    @Test
    @DisplayName("Visualización Exitosa de Metadata")
    void givenScoreP42Exists_whenMetadataIsRequested_thenFullMetadataIsReturnedReactively() {

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

        Mono<Score> resultMono = getScoreService.getMetadataById(SCORE_ID);

        StepVerifier.create(resultMono)
                .expectNextMatches(score ->
                        score.id().equals(SCORE_ID) &&
                                score.title().equals("Título de Prueba") &&
                                score.author().equals("Autor de Prueba") &&
                                score.musicalStyle().equals("Estilo de Prueba")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Fallo 404: Partitura No Encontrada")
    void givenScoreIdNotExists_whenMetadataIsRequested_thenThrowsScoreNotFoundException() {

        final String NON_EXISTENT_ID = "P-99";

        // GIVEN
        when(scoreRepository.findById(NON_EXISTENT_ID))
                .thenReturn(Mono.empty());

        // WHEN
        Mono<Score> resultMono = getScoreService.getMetadataById(NON_EXISTENT_ID);

        // THEN
        StepVerifier.create(resultMono)
                .expectError(ScoreNotFoundException.class)
                .verify();
    }


}