package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.DeleteScoreUseCase;
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
@DisplayName("ScoreControllerDelete Tests")
class ScoreControllerDeleteIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private DeleteScoreUseCase deleteScoreUseCase;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should delete score successfully")
    void shouldDeleteScoreSuccessfully() {
        String scoreId = "score-id-123";
        when(deleteScoreUseCase.deleteScore(eq(scoreId)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/scores/{id}", scoreId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() {
        webTestClient.delete()
                .uri("/api/scores/{id}", "score-id-123")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}