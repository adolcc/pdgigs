package com.pdgigs.infrastructure.adapter.input.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ScoreController - Create Score Authentication Tests")
class ScoreControllerCreateAuthIT {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void createScore_NotAuthenticated_Returns401() {
        // Given
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "My Score");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

        // When & Then
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}