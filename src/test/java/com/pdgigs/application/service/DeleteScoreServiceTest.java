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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteScoreService - Eliminación de partituras")
class DeleteScoreServiceTest {

    private static final String SCORE_ID = "507f1f77bcf86cd799439011";
    private static final String NON_EXISTENT_ID = "507f1f77bcf86cd799439099";
    private static final byte[] VALID_PDF = "%PDF-1.4\nfake".getBytes();

    @Mock
    private ScoreRepository scoreRepository;

    private DeleteScoreService deleteScoreService;

    @BeforeEach
    void setUp() {
        deleteScoreService = new DeleteScoreService(scoreRepository);
    }

    @Test
    @DisplayName("Dado que existe partitura, cuando se elimina, entonces elimina exitosamente")
    void deleteScore_ScoreExists_DeletesSuccessfully() {
        // GIVEN
        Score existingScore = new Score(
                SCORE_ID,
                "Concierto Nº 5",
                "Mozart",
                "Clásico",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));
        when(scoreRepository.deleteById(SCORE_ID))
                .thenReturn(Mono.empty());

        // WHEN & THEN
        StepVerifier.create(deleteScoreService.deleteScore(SCORE_ID))
                .verifyComplete();

        verify(scoreRepository).findById(SCORE_ID);
        verify(scoreRepository).deleteById(SCORE_ID);
    }

    @Test
    @DisplayName("Dado que no existe partitura, cuando se intenta eliminar, entonces lanza ResourceNotFoundException")
    void deleteScore_ScoreNotFound_ThrowsResourceNotFoundException() {
        // GIVEN
        when(scoreRepository.findById(NON_EXISTENT_ID))
                .thenReturn(Mono.empty());

        // WHEN & THEN
        StepVerifier.create(deleteScoreService.deleteScore(NON_EXISTENT_ID))
                .expectErrorMatches(error ->
                        error instanceof ResourceNotFoundException &&
                                error.getMessage().contains("Score not found with ID: " + NON_EXISTENT_ID)
                )
                .verify();

        verify(scoreRepository).findById(NON_EXISTENT_ID);
        verify(scoreRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Dado que existe partitura con PDF grande, cuando se elimina, entonces elimina correctamente")
    void deleteScore_LargePdf_DeletesSuccessfully() {
        // GIVEN
        byte[] largePdf = new byte[10 * 1024 * 1024]; // 10 MB
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
        when(scoreRepository.deleteById(SCORE_ID))
                .thenReturn(Mono.empty());

        // WHEN & THEN
        StepVerifier.create(deleteScoreService.deleteScore(SCORE_ID))
                .verifyComplete();

        verify(scoreRepository).findById(SCORE_ID);
        verify(scoreRepository).deleteById(SCORE_ID);
    }

    @Test
    @DisplayName("Dado que el repositorio falla al buscar, cuando se intenta eliminar, entonces propaga el error")
    void deleteScore_RepositoryFindFailure_PropagatesError() {
        // GIVEN
        RuntimeException repositoryError = new RuntimeException("Database connection lost");
        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.error(repositoryError));

        // WHEN & THEN
        StepVerifier.create(deleteScoreService.deleteScore(SCORE_ID))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Database connection lost")
                )
                .verify();

        verify(scoreRepository).findById(SCORE_ID);
        verify(scoreRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Dado que el repositorio falla al eliminar, cuando se intenta eliminar, entonces propaga el error")
    void deleteScore_RepositoryDeleteFailure_PropagatesError() {
        // GIVEN
        Score existingScore = new Score(
                SCORE_ID,
                "Test",
                "Author",
                "Style",
                VALID_PDF,
                (long) VALID_PDF.length
        );

        RuntimeException deleteError = new RuntimeException("Failed to delete from database");

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));
        when(scoreRepository.deleteById(SCORE_ID))
                .thenReturn(Mono.error(deleteError));

        // WHEN & THEN
        StepVerifier.create(deleteScoreService.deleteScore(SCORE_ID))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Failed to delete from database")
                )
                .verify();

        verify(scoreRepository).findById(SCORE_ID);
        verify(scoreRepository).deleteById(SCORE_ID);
    }
}