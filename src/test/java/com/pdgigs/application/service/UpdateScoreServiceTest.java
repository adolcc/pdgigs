package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateScoreRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private GetScoreMetadataUseCase getScoreMetadataUseCase;

    @InjectMocks
    private ScoreUpdateService scoreUpdateService;

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
                "uploader@example.com",
                LocalDateTime.of(2020, 1, 1, 0, 0)
        );
    }

    @Test
    void given_update_with_only_title_then_only_title_is_changed() {
        when(getScoreMetadataUseCase.findById(eq("S-1"))).thenReturn(Mono.just(existing));
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("uploader@example.com");
        // return an empty list of authorities (non-admin)
        when(auth.getAuthorities()).thenReturn(List.of());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<ReactiveSecurityContextHolder> mocked = mockStatic(ReactiveSecurityContextHolder.class)) {
            mocked.when(ReactiveSecurityContextHolder::getContext).thenReturn(Mono.just(securityContext));

            StepVerifier.create(scoreUpdateService.update("S-1", new UpdateScoreRequest("Concierto", null, null)))
                    .assertNext(updated -> {
                        assertThat(updated.id()).isEqualTo("S-1");
                        assertThat(updated.title()).isEqualTo("Concierto");
                        assertThat(updated.author()).isEqualTo("Bach");
                        assertThat(updated.musicStyle()).isEqualTo("Barroco");
                        assertThat(updated.filename()).isEqualTo("stored-file.pdf");
                        assertThat(updated.userEmail()).isEqualTo("uploader@example.com");
                        assertThat(updated.createdAt()).isEqualTo(existing.createdAt());
                    })
                    .verifyComplete();

            verify(scoreRepository).save(scoreCaptor.capture());
            Score saved = scoreCaptor.getValue();
            assertThat(saved.title()).isEqualTo("Concierto");
            assertThat(saved.author()).isEqualTo("Bach");
            assertThat(saved.userEmail()).isEqualTo("uploader@example.com");
        }
    }

    @Test
    void given_update_with_author_and_style_then_title_is_preserved() {
        when(getScoreMetadataUseCase.findById(eq("S-1"))).thenReturn(Mono.just(existing));
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("uploader@example.com");
        when(auth.getAuthorities()).thenReturn(List.of());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<ReactiveSecurityContextHolder> mocked = mockStatic(ReactiveSecurityContextHolder.class)) {
            mocked.when(ReactiveSecurityContextHolder::getContext).thenReturn(Mono.just(securityContext));

            StepVerifier.create(scoreUpdateService.update("S-1", new UpdateScoreRequest(null, "Mozart", "Clásico")))
                    .assertNext(updated -> {
                        assertThat(updated.title()).isEqualTo("Melodía");
                        assertThat(updated.author()).isEqualTo("Mozart");
                        assertThat(updated.musicStyle()).isEqualTo("Clásico");
                        assertThat(updated.userEmail()).isEqualTo("uploader@example.com");
                    })
                    .verifyComplete();

            verify(scoreRepository).save(scoreCaptor.capture());
            Score saved = scoreCaptor.getValue();
            assertThat(saved.title()).isEqualTo("Melodía");
            assertThat(saved.author()).isEqualTo("Mozart");
            assertThat(saved.musicStyle()).isEqualTo("Clásico");
            assertThat(saved.userEmail()).isEqualTo("uploader@example.com");
        }
    }

    @Test
    void given_update_with_all_fields_then_all_are_updated() {
        when(getScoreMetadataUseCase.findById(eq("S-1"))).thenReturn(Mono.just(existing));
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("uploader@example.com");
        when(auth.getAuthorities()).thenReturn(List.of());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<ReactiveSecurityContextHolder> mocked = mockStatic(ReactiveSecurityContextHolder.class)) {
            mocked.when(ReactiveSecurityContextHolder::getContext).thenReturn(Mono.just(securityContext));

            StepVerifier.create(scoreUpdateService.update("S-1", new UpdateScoreRequest("Obertura", "Haydn", "Clásico")))
                    .assertNext(updated -> {
                        assertThat(updated.title()).isEqualTo("Obertura");
                        assertThat(updated.author()).isEqualTo("Haydn");
                        assertThat(updated.musicStyle()).isEqualTo("Clásico");
                        assertThat(updated.userEmail()).isEqualTo("uploader@example.com");
                    })
                    .verifyComplete();

            verify(scoreRepository).save(scoreCaptor.capture());
            Score saved = scoreCaptor.getValue();
            assertThat(saved.title()).isEqualTo("Obertura");
            assertThat(saved.author()).isEqualTo("Haydn");
            assertThat(saved.musicStyle()).isEqualTo("Clásico");
            assertThat(saved.userEmail()).isEqualTo("uploader@example.com");
        }
    }

    @Test
    void given_nonexistent_score_then_returns_empty_and_no_save() {
        when(getScoreMetadataUseCase.findById(eq("S-404"))).thenReturn(Mono.empty());

        StepVerifier.create(scoreUpdateService.update("S-404", new UpdateScoreRequest("Any", null, null)))
                .expectNextCount(0)
                .verifyComplete();

        verify(scoreRepository, never()).save(any());
    }
}