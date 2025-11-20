package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ScoreControllerGet Tests")
class ScoreControllerGetIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GetScoreMetadataUseCase getScoreMetadataUseCase;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should get score metadata by ID")
    void shouldGetScoreMetadataById() {
        String scoreId = "674b8e1234567890abcdef12";
        Score mockScore = new Score(
                scoreId,
                "Test Score",
                "John Composer",
                "Classical",
                new byte[]{0x25, 0x50, 0x44, 0x46},
                12345L
        );

        when(getScoreMetadataUseCase.getMetadataById(eq(scoreId)))
                .thenReturn(Mono.just(mockScore));

        webTestClient.get()
                .uri("/api/scores/{id}", scoreId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(scoreId)
                .jsonPath("$.title").isEqualTo("Test Score");
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() {
        webTestClient.get()
                .uri("/api/scores/{id}", "674b8e1234567890abcdef12")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}