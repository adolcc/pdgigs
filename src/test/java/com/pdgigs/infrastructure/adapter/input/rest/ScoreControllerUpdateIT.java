package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.UpdateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ScoreController - Update Score Integration Tests")
class ScoreControllerUpdateIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UpdateScoreUseCase updateScoreUseCase;

    @MockitoBean
    private ScoreRestMapper scoreRestMapper;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should update score metadata successfully and return 200")
    void updateScoreMetadata_ValidRequest_Returns200() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";
        Score updatedScore = new Score(
                scoreId, "Updated Title", "Updated Author", "Classical",
                new byte[]{}, 1024L, "user123", "user@example.com", null
        );
        ScoreResponse response = new ScoreResponse(
                scoreId, "Updated Title", "Updated Author", "Classical", 1024L, null
        );

        when(updateScoreUseCase.updateMetadata(eq(scoreId), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(updatedScore));
        when(scoreRestMapper.toResponse(updatedScore)).thenReturn(response);

        String requestBody = """
                {
                    "title": "Updated Title",
                    "author": "Updated Author", 
                    "musicalStyle": "Classical"
                }
                """;

        // When & Then
        webTestClient.put()
                .uri("/api/scores/{id}", scoreId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(scoreId)
                .jsonPath("$.title").isEqualTo("Updated Title")
                .jsonPath("$.author").isEqualTo("Updated Author");

        verify(updateScoreUseCase).updateMetadata(scoreId, "Updated Title", "Updated Author", "Classical");
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void updateScoreMetadata_NotAuthenticated_Returns401() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";

        // When & Then
        webTestClient.put()
                .uri("/api/scores/{id}", scoreId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}