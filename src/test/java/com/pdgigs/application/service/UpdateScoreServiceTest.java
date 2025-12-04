package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private UpdateScoreService updateScoreService;

    @Captor
    private ArgumentCaptor<Score> scoreCaptor;

    private Score existing;

    @BeforeEach
    void setUp() {
        existing = new Score(
                "S-1",
                "Melodía",
                "Bach",
                "Barroco",
                "stored-file.pdf",
                LocalDateTime.of(2020, 1, 1, 0, 0)
        );
    }

    @Test
    void given_update_with_only_title_then_only_title_is_changed() {
        when(scoreRepository.findById(eq("S-1"))).thenReturn(Mono.just(existing));
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(updateScoreService.updateMetadata("S-1", "Concierto", null, null))
                .assertNext(updated -> {
                    assertThat(updated.id()).isEqualTo("S-1");
                    assertThat(updated.title()).isEqualTo("Concierto");
                    assertThat(updated.author()).isEqualTo("Bach");
                    assertThat(updated.musicStyle()).isEqualTo("Barroco");
                    assertThat(updated.filename()).isEqualTo("stored-file.pdf");
                    assertThat(updated.createdAt()).isEqualTo(existing.createdAt());
                })
                .verifyComplete();

        verify(scoreRepository).save(scoreCaptor.capture());
        Score saved = scoreCaptor.getValue();
        assertThat(saved.title()).isEqualTo("Concierto");
        assertThat(saved.author()).isEqualTo("Bach");
    }

    @Test
    void given_update_with_author_and_style_then_title_is_preserved() {
        when(scoreRepository.findById(eq("S-1"))).thenReturn(Mono.just(existing));
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(updateScoreService.updateMetadata("S-1", null, "Mozart", "Clásico"))
                .assertNext(updated -> {
                    assertThat(updated.title()).isEqualTo("Melodía");
                    assertThat(updated.author()).isEqualTo("Mozart");
                    assertThat(updated.musicStyle()).isEqualTo("Clásico");
                })
                .verifyComplete();

        verify(scoreRepository).save(scoreCaptor.capture());
        Score saved = scoreCaptor.getValue();
        assertThat(saved.title()).isEqualTo("Melodía");
        assertThat(saved.author()).isEqualTo("Mozart");
        assertThat(saved.musicStyle()).isEqualTo("Clásico");
    }

    @Test
    void given_update_with_all_fields_then_all_are_updated() {
        when(scoreRepository.findById(eq("S-1"))).thenReturn(Mono.just(existing));
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(updateScoreService.updateMetadata("S-1", "Obertura", "Haydn", "Clásico"))
                .assertNext(updated -> {
                    assertThat(updated.title()).isEqualTo("Obertura");
                    assertThat(updated.author()).isEqualTo("Haydn");
                    assertThat(updated.musicStyle()).isEqualTo("Clásico");
                })
                .verifyComplete();

        verify(scoreRepository).save(scoreCaptor.capture());
        Score saved = scoreCaptor.getValue();
        assertThat(saved.title()).isEqualTo("Obertura");
        assertThat(saved.author()).isEqualTo("Haydn");
        assertThat(saved.musicStyle()).isEqualTo("Clásico");
    }

    @Test
    void given_nonexistent_score_then_throws_not_found() {
        when(scoreRepository.findById(eq("S-404"))).thenReturn(Mono.empty());

        StepVerifier.create(updateScoreService.updateMetadata("S-404", "Any", null, null))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().contains("S-404"))
                .verify();

        verify(scoreRepository, never()).save(any());
    }
}