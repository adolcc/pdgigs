package com.pdgigs.infrastructure.adapter.input.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ScoreController - Download Score Authentication Tests")
class ScoreControllerDownloadAuthIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void downloadScorePdf_NotAuthenticated_Returns401() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";

        // When & Then
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}