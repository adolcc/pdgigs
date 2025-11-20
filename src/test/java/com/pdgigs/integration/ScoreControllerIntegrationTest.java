package com.pdgigs.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;

@DisplayName("ScoreController Integration Tests with JWT Authentication")
class ScoreControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should upload score successfully with valid JWT token")
    void shouldUploadScoreWithAuthentication() throws Exception {
        // Given
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "Integration Test Score");
        builder.part("author", "Adolfo");
        builder.part("musicalStyle", "Jazz");

        // When & Then
        webTestClient.post()
                .uri("/api/scores/upload")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.title").isEqualTo("Integration Test Score")
                .jsonPath("$.author").isEqualTo("Adolfo")
                .jsonPath("$.musicalStyle").isEqualTo("Jazz")
                .jsonPath("$.fileSize").exists();
    }

    @Test
    @DisplayName("Should return 401 when uploading score without JWT token")
    void shouldReturn401WhenUploadingWithoutToken() throws Exception {
        // Given
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "Test Score");
        builder.part("author", "Author");
        builder.part("musicalStyle", "Classical");

        // When & Then
        webTestClient.post()
                .uri("/api/scores/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should return 401 when using invalid JWT token")
    void shouldReturn401WithInvalidToken() throws Exception {
        // Given
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "Test Score");
        builder.part("author", "Author");
        builder.part("musicalStyle", "Classical");

        // When & Then
        webTestClient.post()
                .uri("/api/scores/upload")
                .header("Authorization", "Bearer invalid-token-xyz")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}