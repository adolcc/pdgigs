package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.CreateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.DomainExceptionHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.GlobalFallbackHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.ValidationExceptionHandler;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.config.SecurityConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import({
        ScoreRestMapper.class,
        SecurityConfig.class,
        DomainExceptionHandler.class,
        ValidationExceptionHandler.class,
        GlobalFallbackHandler.class
})
@DisplayName("ScoreController - Create Score Tests")
class ScoreControllerCreateIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateScoreUseCase createScoreUseCase;

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should create score with complete metadata and return 201")
    void createScore_CompleteMetadata_Returns201() throws Exception {
        Score mockScore = new Score(
                "score-id-123",
                "My Score",
                "John Doe",
                "Jazz",
                new byte[]{0x25, 0x50, 0x44, 0x46},
                12345L,
                "user123",
                "test@example.com",
                null
        );

        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockScore));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "My Score");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

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

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should fail when file is missing")
    void createScore_MissingFile_Returns400() {
        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new com.pdgigs.domain.exception.validation.FileValidationError.Empty().toException()));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        ByteArrayResource emptyPdf = new ByteArrayResource(new byte[0]) {
            @Override
            public String getFilename() {
                return "empty.pdf";
            }
        };
        builder.part("file", emptyPdf);
        builder.part("title", "My Score");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should fail when title is empty")
    void createScore_EmptyTitle_Returns400() throws Exception {
        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new com.pdgigs.domain.exception.validation.FileValidationError.Empty().toException()));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should fail when author is empty")
    void createScore_EmptyAuthor_Returns400() throws Exception {
        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new com.pdgigs.domain.exception.validation.FileValidationError.Empty().toException()));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "My Score");
        builder.part("author", "");
        builder.part("musicalStyle", "Jazz");

        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void createScore_NotAuthenticated_Returns401() throws Exception {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "My Score");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}