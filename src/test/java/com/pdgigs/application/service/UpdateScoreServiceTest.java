package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ScoreNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateScoreService - actualización de metadata")
class UpdateScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    private UpdateScoreService updateScoreService;

    private Score existing;

    @BeforeEach
    void setUp() {
        updateScoreService = new UpdateScoreService(scoreRepository);
        byte[] pdfContent = "%PDF-1.4\nfake".getBytes();
        existing = new Score("1", "Melodía", "Bach", "Barroco", pdfContent, (long) pdfContent.length);
    }

    @Test
    @DisplayName("Actualización parcial: solo título")
    void updatePartial_TitleOnly() {
        when(scoreRepository.findById("1")).thenReturn(Mono.just(existing));
        Score updated = new Score("1", "Concierto", "Bach", "Barroco", existing.pdfContent(), existing.fileSize());
        when(scoreRepository.save(any(Score.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(updateScoreService.updateMetadata("1", "Concierto", null, null))
                .expectNextMatches(s -> "Concierto".equals(s.title())
                        && "Bach".equals(s.author())
                        && "Barroco".equals(s.musicalStyle()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Actualización múltiple: autor y estilo")
    void updateMultiple_AuthorAndStyle() {
        when(scoreRepository.findById("1")).thenReturn(Mono.just(existing));
        Score updated = new Score("1", "Melodía", "Mozart", "Clásico", existing.pdfContent(), existing.fileSize());
        when(scoreRepository.save(any(Score.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(updateScoreService.updateMetadata("1", null, "Mozart", "Clásico"))
                .expectNextMatches(s -> "Melodía".equals(s.title())
                        && "Mozart".equals(s.author())
                        && "Clásico".equals(s.musicalStyle()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Actualización completa: todos los campos")
    void updateAll_AllFields() {
        when(scoreRepository.findById("1")).thenReturn(Mono.just(existing));
        Score updated = new Score("1", "Nueva", "NuevoAutor", "NuevoEstilo", existing.pdfContent(), existing.fileSize());
        when(scoreRepository.save(any(Score.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(updateScoreService.updateMetadata("1", "Nueva", "NuevoAutor", "NuevoEstilo"))
                .expectNextMatches(s -> "Nueva".equals(s.title())
                        && "NuevoAutor".equals(s.author())
                        && "NuevoEstilo".equals(s.musicalStyle()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Score no encontrado -> 404 (exception)")
    void update_NotFound() {
        when(scoreRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(updateScoreService.updateMetadata("missing", "X", null, null))
                .expectErrorMatches(throwable -> throwable instanceof ScoreNotFoundException
                        && throwable.getMessage().contains("missing"))
                .verify();
    }
}
