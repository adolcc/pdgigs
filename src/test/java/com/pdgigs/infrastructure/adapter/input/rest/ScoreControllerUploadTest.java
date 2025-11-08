package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.port.in.UploadScoreUseCase;
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
import static org.mockito.Mockito.when;

@WebFluxTest(ScoreController.class)
@Import({ScoreRestMapper.class, SecurityConfig.class})
@DisplayName("Controller: Subida exitosa de partituras")
public class ScoreControllerUploadTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UploadScoreUseCase uploadScoreUseCase;

    @Test
    @DisplayName("POST /api/scores - Debe subir partitura con metadata completa")
    void uploadScore_WithCompleteMetadata_ShouldReturn201() {
        // GIVEN
        var mockScore = ScoreMockFactory.createWithCompleteMetadata("123");
        when(uploadScoreUseCase.uploadScore(any(byte[].class), anyString(), anyString(), anyString()))
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
                .jsonPath("$.id").isEqualTo("123")
                .jsonPath("$.title").isEqualTo("Concierto Nº 5")
                .jsonPath("$.author").isEqualTo("Mozart")
                .jsonPath("$.musicalStyle").isEqualTo("Clásico");
    }

    @Test
    @DisplayName("POST /api/scores - Debe subir partitura con metadata vacía")
    void uploadScore_WithEmptyMetadata_ShouldReturn201() {
        // GIVEN
        var mockScore = ScoreMockFactory.createWithEmptyMetadata("456");
        when(uploadScoreUseCase.uploadScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockScore));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithEmptyMetadata().build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("456")
                .jsonPath("$.title").isEqualTo("")
                .jsonPath("$.author").isEqualTo("")
                .jsonPath("$.musicalStyle").isEqualTo("");
    }

    @Test
    @DisplayName("POST /api/scores - Debe subir partitura con metadata parcial")
    void uploadScore_WithPartialMetadata_ShouldReturn201() {
        // GIVEN
        var mockScore = ScoreMockFactory.createWithPartialMetadata("789", "Concierto Nº 5");
        when(uploadScoreUseCase.uploadScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockScore));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithPartialMetadata("Concierto Nº 5").build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Concierto Nº 5")
                .jsonPath("$.author").isEqualTo("")
                .jsonPath("$.musicalStyle").isEqualTo("");
    }
}