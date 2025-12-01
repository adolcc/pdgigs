package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.CreateScoreUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ScoreController - Create Score Validation Tests")
class ScoreControllerCreateValidationIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateScoreUseCase createScoreUseCase;

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should return 400 when file is missing")
    void createScore_MissingFile_Returns400() {
        // Given
        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("File is required")));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("title", "My Score");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

        // When & Then
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should return 400 when file is empty")
    void createScore_EmptyFile_Returns400() {
        // Given
        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("File cannot be empty")));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(new byte[0]) {
            @Override
            public String getFilename() {
                return "empty.pdf";
            }
        });
        builder.part("title", "My Score");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

        // When & Then
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should return 400 when title is empty")
    void createScore_EmptyTitle_Returns400() {
        // Given
        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Title cannot be empty")));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

        // When & Then
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should return 400 when author is empty")
    void createScore_EmptyAuthor_Returns400() {
        // Given
        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Author cannot be empty")));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "My Score");
        builder.part("author", "");
        builder.part("musicalStyle", "Jazz");

        // When & Then
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
    }
}