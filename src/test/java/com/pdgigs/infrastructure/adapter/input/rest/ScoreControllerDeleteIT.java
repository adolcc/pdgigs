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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ScoreController - Delete Score Integration Tests")
class ScoreControllerDeleteIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private DeleteScoreUseCase deleteScoreUseCase;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should delete score successfully and return 204")
    void deleteScore_ValidId_Returns204() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";
        when(deleteScoreUseCase.deleteScore(eq(scoreId)))
                .thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/scores/{id}", scoreId)
                .exchange()
                .expectStatus().isNoContent();

        // Verify interaction
        verify(deleteScoreUseCase).deleteScore(scoreId);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should handle non-existent score and return 404")
    void deleteScore_NonExistentScore_Returns404() {
        // Given
        String nonExistentId = "000000000000000000000000";
        when(deleteScoreUseCase.deleteScore(eq(nonExistentId)))
                .thenReturn(Mono.error(new RuntimeException("Score not found")));

        // When & Then
        webTestClient.delete()
                .uri("/api/scores/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void deleteScore_NotAuthenticated_Returns401() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";

        // When & Then
        webTestClient.delete()
                .uri("/api/scores/{id}", scoreId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should return 400 when ID format is invalid")
    void deleteScore_InvalidIdFormat_Returns400() {
        // Given
        String invalidId = "invalid-id-format";
        when(deleteScoreUseCase.deleteScore(eq(invalidId)))
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid ID format")));

        // When & Then
        webTestClient.delete()
                .uri("/api/scores/{id}", invalidId)
                .exchange()
                .expectStatus().isBadRequest();
    }
}