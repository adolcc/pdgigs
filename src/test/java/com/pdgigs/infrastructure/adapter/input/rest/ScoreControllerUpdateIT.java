package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.UpdateScoreUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ScoreControllerUpdate Tests")
class ScoreControllerUpdateIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UpdateScoreUseCase updateScoreUseCase;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should update score successfully")
    void shouldUpdateScoreSuccessfully() {
        String scoreId = "score-id-123";
        Score updatedScore = new Score(
                scoreId,
                "Updated Title",
                "Updated Author",
                "Classical",
                new byte[]{0x25, 0x50, 0x44, 0x46},
                12345L
        );

        when(updateScoreUseCase.updateMetadata(eq(scoreId), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(updatedScore));

        String requestBody = """
                {
                    "title": "Updated Title",
                    "author": "Updated Author",
                    "musicalStyle": "Classical"
                }
                """;

        webTestClient.put()
                .uri("/api/scores/{id}", scoreId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() {
        webTestClient.put()
                .uri("/api/scores/{id}", "score-id-123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}