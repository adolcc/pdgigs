package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.CreateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.DomainExceptionHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.GlobalFallbackHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.ValidationExceptionHandler;
import com.pdgigs.infrastructure.adapter.input.rest.helper.MultipartRequestFactory;
import com.pdgigs.infrastructure.adapter.input.rest.helper.ScoreMockFactory;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ScoreControllerCreate.class)
@Import({
        ScoreRestMapper.class,
        SecurityConfig.class,
        DomainExceptionHandler.class,
        ValidationExceptionHandler.class,
        GlobalFallbackHandler.class
})
@DisplayName("Controller: Creación de partituras")
class ScoreControllerCreateTest {

    private static final String SCORE_ID = "507f1f77bcf86cd799439011";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateScoreUseCase createScoreUseCase;

    @Test
    @DisplayName("POST /api/scores - Metadata completa → 201")
    void createScore_CompleteMetadata_Returns201() {
        // GIVEN
        Score mockScore = ScoreMockFactory.createWithCompleteMetadata(SCORE_ID);

        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockScore));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithCompleteMetadata().build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(SCORE_ID)
                .jsonPath("$.title").isNotEmpty()
                .jsonPath("$.author").isNotEmpty()
                .jsonPath("$.musicalStyle").isNotEmpty()
                .jsonPath("$.fileSize").isNumber();

        verify(createScoreUseCase).createScore(any(byte[].class), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/scores - Solo título → 201")
    void createScore_OnlyTitle_Returns201() {
        // GIVEN
        Score mockScore = ScoreMockFactory.create(SCORE_ID, "Solo Título", "", "");

        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockScore));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithCompleteMetadata().build()))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("POST /api/scores - Sin metadata → 201")
    void createScore_NoMetadata_Returns201() {
        // GIVEN
        Score mockScore = ScoreMockFactory.create(SCORE_ID, "", "", "");

        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockScore));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithCompleteMetadata().build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(SCORE_ID);
    }
}