package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateScoreService - Actualización de metadata")
class UpdateScoreServiceTest {

    private static final String SCORE_ID = "507f1f77bcf86cd799439011";  // MongoDB ObjectId válido
    private static final String NON_EXISTENT_ID = "507f1f77bcf86cd799439099";
    private static final byte[] PDF_CONTENT = "%PDF-1.4\nfake content".getBytes();

    @Mock
    private ScoreRepository scoreRepository;

    private UpdateScoreService updateScoreService;
    private Score existingScore;

    @BeforeEach
    void setUp() {
        updateScoreService = new UpdateScoreService(scoreRepository);

        existingScore = new Score(
                SCORE_ID,
                "Melodía Original",
                "Johann Sebastian Bach",
                "Barroco",
                PDF_CONTENT,
                (long) PDF_CONTENT.length
        );
    }

    @Test
    @DisplayName("Dado que existe una partitura, cuando se actualiza solo el título, entonces solo el título cambia")
    void givenExistingScore_whenUpdateTitleOnly_thenOnlyTitleIsUpdated() {
        // GIVEN
        String newTitle = "Concierto de Brandenburgo";

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));

        when(scoreRepository.save(any(Score.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // WHEN
        Mono<Score> resultMono = updateScoreService.updateMetadata(SCORE_ID, newTitle, null, null);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(updated -> {
                    assertThat(updated.id()).isEqualTo(SCORE_ID);
                    assertThat(updated.title()).isEqualTo(newTitle);
                    assertThat(updated.author()).isEqualTo("Johann Sebastian Bach");  // Sin cambios
                    assertThat(updated.musicalStyle()).isEqualTo("Barroco");  // Sin cambios
                    assertThat(updated.pdfContent()).isEqualTo(PDF_CONTENT);  // Sin cambios
                    assertThat(updated.fileSize()).isEqualTo((long) PDF_CONTENT.length);
                })
                .verifyComplete();

        verify(scoreRepository).findById(SCORE_ID);
        verify(scoreRepository).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado que existe una partitura, cuando se actualiza autor y estilo, entonces solo esos campos cambian")
    void givenExistingScore_whenUpdateAuthorAndStyle_thenOnlyThoseFieldsAreUpdated() {
        // GIVEN
        String newAuthor = "Wolfgang Amadeus Mozart";
        String newStyle = "Clásico";

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));

        when(scoreRepository.save(any(Score.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // WHEN
        Mono<Score> resultMono = updateScoreService.updateMetadata(SCORE_ID, null, newAuthor, newStyle);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(updated -> {
                    assertThat(updated.title()).isEqualTo("Melodía Original");  // Sin cambios
                    assertThat(updated.author()).isEqualTo(newAuthor);
                    assertThat(updated.musicalStyle()).isEqualTo(newStyle);
                })
                .verifyComplete();

        verify(scoreRepository).findById(SCORE_ID);
        verify(scoreRepository).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado que existe una partitura, cuando se actualizan todos los campos, entonces todos cambian")
    void givenExistingScore_whenUpdateAllFields_thenAllFieldsAreUpdated() {
        // GIVEN
        String newTitle = "Sinfonía No. 40";
        String newAuthor = "Wolfgang Amadeus Mozart";
        String newStyle = "Clásico";

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));

        when(scoreRepository.save(any(Score.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // WHEN
        Mono<Score> resultMono = updateScoreService.updateMetadata(SCORE_ID, newTitle, newAuthor, newStyle);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(updated -> {
                    assertThat(updated.id()).isEqualTo(SCORE_ID);
                    assertThat(updated.title()).isEqualTo(newTitle);
                    assertThat(updated.author()).isEqualTo(newAuthor);
                    assertThat(updated.musicalStyle()).isEqualTo(newStyle);
                    assertThat(updated.pdfContent()).isEqualTo(PDF_CONTENT);
                    assertThat(updated.fileSize()).isEqualTo((long) PDF_CONTENT.length);
                })
                .verifyComplete();

        ArgumentCaptor<Score> scoreCaptor = ArgumentCaptor.forClass(Score.class);
        verify(scoreRepository).save(scoreCaptor.capture());

        Score savedScore = scoreCaptor.getValue();
        assertThat(savedScore.title()).isEqualTo(newTitle);
        assertThat(savedScore.author()).isEqualTo(newAuthor);
        assertThat(savedScore.musicalStyle()).isEqualTo(newStyle);
    }

    @Test
    @DisplayName("Dado que existe una partitura, cuando no se actualiza ningún campo, entonces permanece sin cambios")
    void givenExistingScore_whenNoFieldsUpdated_thenScoreRemainsUnchanged() {
        // GIVEN
        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));

        when(scoreRepository.save(any(Score.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // WHEN
        Mono<Score> resultMono = updateScoreService.updateMetadata(SCORE_ID, null, null, null);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(updated -> {
                    assertThat(updated.id()).isEqualTo(SCORE_ID);
                    assertThat(updated.title()).isEqualTo("Melodía Original");
                    assertThat(updated.author()).isEqualTo("Johann Sebastian Bach");
                    assertThat(updated.musicalStyle()).isEqualTo("Barroco");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Dado que el PDF original existe, cuando se actualiza metadata, entonces el PDF no se modifica")
    void givenExistingPdf_whenMetadataUpdated_thenPdfIsNotModified() {
        // GIVEN
        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));

        when(scoreRepository.save(any(Score.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // WHEN
        Mono<Score> resultMono = updateScoreService.updateMetadata(SCORE_ID, "Nuevo Título", null, null);

        // THEN
        StepVerifier.create(resultMono)
                .assertNext(updated -> {
                    assertThat(updated.pdfContent()).isEqualTo(PDF_CONTENT);
                    assertThat(updated.fileSize()).isEqualTo((long) PDF_CONTENT.length);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Fallo 404: Dado que el ID no existe, cuando se intenta actualizar, entonces lanza ResourceNotFoundException")
    void givenNonExistentId_whenUpdateMetadata_thenThrowsResourceNotFoundException() {
        // GIVEN
        when(scoreRepository.findById(NON_EXISTENT_ID))
                .thenReturn(Mono.empty());

        // WHEN
        Mono<Score> resultMono = updateScoreService.updateMetadata(NON_EXISTENT_ID, "Nuevo Título", null, null);

        // THEN
        StepVerifier.create(resultMono)
                .expectErrorMatches(error ->
                        error instanceof ResourceNotFoundException &&
                                error.getMessage().contains("Score not found with ID: " + NON_EXISTENT_ID)
                )
                .verify();

        verify(scoreRepository).findById(NON_EXISTENT_ID);
        verify(scoreRepository, never()).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado que el repositorio falla al buscar, cuando se intenta actualizar, entonces propaga el error")
    void givenRepositoryFindFailure_whenUpdateMetadata_thenErrorIsPropagated() {
        // GIVEN
        RuntimeException repositoryError = new RuntimeException("Database connection lost");
        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.error(repositoryError));

        // WHEN
        Mono<Score> resultMono = updateScoreService.updateMetadata(SCORE_ID, "Nuevo Título", null, null);

        // THEN
        StepVerifier.create(resultMono)
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Database connection lost")
                )
                .verify();

        verify(scoreRepository).findById(SCORE_ID);
        verify(scoreRepository, never()).save(any(Score.class));
    }

    @Test
    @DisplayName("Dado que el repositorio falla al guardar, cuando se intenta actualizar, entonces propaga el error")
    void givenRepositorySaveFailure_whenUpdateMetadata_thenErrorIsPropagated() {
        // GIVEN
        RuntimeException saveError = new RuntimeException("Failed to save to database");

        when(scoreRepository.findById(SCORE_ID))
                .thenReturn(Mono.just(existingScore));

        when(scoreRepository.save(any(Score.class)))
                .thenReturn(Mono.error(saveError));

        // WHEN
        Mono<Score> resultMono = updateScoreService.updateMetadata(SCORE_ID, "Nuevo Título", null, null);

        // THEN
        StepVerifier.create(resultMono)
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Failed to save to database")
                )
                .verify();

        verify(scoreRepository).findById(SCORE_ID);
        verify(scoreRepository).save(any(Score.class));
    }
}