package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.CreateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
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
@DisplayName("ScoreController - Create Score Success Tests")
class ScoreControllerCreateSuccessIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateScoreUseCase createScoreUseCase;

    @MockitoBean
    private ScoreRestMapper scoreRestMapper;

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should create score with complete metadata and return 201")
    void createScore_CompleteMetadata_Returns201() {
        // Given
        byte[] pdfContent = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
        Score mockScore = new Score(
                "score-id-123", "My Score", "John Doe", "Jazz",
                pdfContent, 12345L, "user123", "test@example.com", null
        );

        ScoreResponse mockResponse = new ScoreResponse(
                "score-id-123", "My Score", "John Doe", "Jazz", 12345L, null
        );

        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockScore));
        when(scoreRestMapper.toResponse(mockScore)).thenReturn(mockResponse);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(pdfContent) {
            @Override
            public String getFilename() {
                return "test-score.pdf";
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
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("score-id-123")
                .jsonPath("$.title").isEqualTo("My Score")
                .jsonPath("$.author").isEqualTo("John Doe")
                .jsonPath("$.musicalStyle").isEqualTo("Jazz")
                .jsonPath("$.fileSize").isEqualTo(12345);
    }
}