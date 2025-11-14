package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ScoreNotFoundException;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service: Eliminación de partituras")
class DeleteScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    private DeleteScoreService deleteScoreService;

    @BeforeEach
    void setUp() {
        deleteScoreService = new DeleteScoreService(scoreRepository);
    }

    @Test
    @DisplayName("Debe eliminar partitura exitosamente cuando existe")
    void deleteScore_WhenScoreExists_ShouldDeleteSuccessfully() {
        // GIVEN
        String scoreId = "P-55";
        Score existingScore = new Score(
                scoreId,
                "Test Score",
                "Test Author",
                "Classical",
                new byte[]{1, 2, 3},
                3L
        );

        when(scoreRepository.findById(scoreId)).thenReturn(Mono.just(existingScore));
        when(scoreRepository.deleteById(scoreId)).thenReturn(Mono.empty());

        // WHEN & THEN
        StepVerifier.create(deleteScoreService.deleteScore(scoreId))
                .verifyComplete();

        verify(scoreRepository, times(1)).findById(scoreId);
        verify(scoreRepository, times(1)).deleteById(scoreId);
    }

    @Test
    @DisplayName("Debe lanzar ScoreNotFoundException cuando la partitura no existe")
    void deleteScore_WhenScoreNotFound_ShouldThrowException() {
        // GIVEN
        String scoreId = "P-99";
        when(scoreRepository.findById(scoreId)).thenReturn(Mono.empty());

        // WHEN & THEN
        StepVerifier.create(deleteScoreService.deleteScore(scoreId))
                .expectErrorMatches(error ->
                        error instanceof ScoreNotFoundException &&
                                error.getMessage().contains("Score with ID P-99 not found.")
                )
                .verify();

        verify(scoreRepository, times(1)).findById(scoreId);
        verify(scoreRepository, never()).deleteById(anyString());
    }
}