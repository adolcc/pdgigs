package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
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
@DisplayName("ScoreController - Download Score Error Tests")
class ScoreControllerDownloadErrorIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GetScorePdfUseCase getScorePdfUseCase;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should return 404 when score not found")
    void downloadScorePdf_ScoreNotFound_Returns404() {
        // Given
        String nonExistentId = "000000000000000000000000";
        when(getScorePdfUseCase.getPdfContentById(eq(nonExistentId)))
                .thenReturn(Mono.error(ResourceNotFoundException.score(nonExistentId)));

        // When & Then
        webTestClient.get()
                .uri("/api/scores/{id}/download", nonExistentId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("RESOURCE_NOT_FOUND")
                .jsonPath("$.message").value(message ->
                        message.toString().contains("Score not found with ID: " + nonExistentId));

        verify(getScorePdfUseCase).getPdfContentById(nonExistentId);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should return 400 when ID format is invalid")
    void downloadScorePdf_InvalidIdFormat_Returns400() {
        // Given
        String invalidId = "invalid-id-format";
        when(getScorePdfUseCase.getPdfContentById(eq(invalidId)))
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid ID format")));

        // When & Then
        webTestClient.get()
                .uri("/api/scores/{id}/download", invalidId)
                .exchange()
                .expectStatus().isBadRequest();

        verify(getScorePdfUseCase).getPdfContentById(invalidId);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should return 500 when internal error occurs")
    void downloadScorePdf_InternalError_Returns500() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";
        when(getScorePdfUseCase.getPdfContentById(eq(scoreId)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(getScorePdfUseCase).getPdfContentById(scoreId);
    }
}