package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GetScoreMetadataServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private GetScoreMetadataService getScoreMetadataService;

    private Score sampleScore;

    @BeforeEach
    void setUp() {
        sampleScore = new Score(
                "P-42",
                "Symphony No.5",
                "Ludwig van Beethoven",
                "Classical",
                "stored-file.pdf",
                "uploader@example.com",
                LocalDateTime.now()
        );
    }

    @Test
    void given_existing_score_when_findById_then_return_score() {
        when(scoreRepository.findById(eq("P-42"))).thenReturn(Mono.just(sampleScore));

        StepVerifier.create(getScoreMetadataService.findById("P-42"))
                .assertNext(score -> {
                    assertThat(score.id()).isEqualTo("P-42");
                    assertThat(score.title()).isEqualTo("Symphony No.5");
                    assertThat(score.author()).isEqualTo("Ludwig van Beethoven");
                    assertThat(score.musicStyle()).isEqualTo("Classical");
                    assertThat(score.filename()).isEqualTo("stored-file.pdf");
                    assertThat(score.userEmail()).isEqualTo("uploader@example.com");
                })
                .verifyComplete();
    }

    @Test
    void given_missing_score_when_findById_then_return_not_found_error() {
        when(scoreRepository.findById(eq("P-99"))).thenReturn(Mono.empty());

        StepVerifier.create(getScoreMetadataService.findById("P-99"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResourceNotFoundException &&
                                throwable.getMessage().contains("P-99")
                )
                .verify();
    }
}