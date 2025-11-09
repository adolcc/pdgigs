package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.port.input.UploadScoreUseCase;
import com.pdgigs.domain.exception.FileSizeExceededException;
import com.pdgigs.domain.exception.InvalidFileFormatException;
import com.pdgigs.infrastructure.adapter.input.rest.helper.MultipartRequestFactory;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
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
@DisplayName("Controller: Validaciones de formato y tamaño")
class ScoreControllerValidationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UploadScoreUseCase uploadScoreUseCase;

    @Test
    @DisplayName("POST /api/scores - Debe rechazar archivo no PDF con HTTP 415")
    void uploadScore_WithInvalidFormat_ShouldReturn415() {
        // GIVEN
        when(uploadScoreUseCase.uploadScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new InvalidFileFormatException("File format not allowed")));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithInvalidFile().build()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .expectBody()
                .jsonPath("$.message").isEqualTo("File format not allowed")
                .jsonPath("$.status").isEqualTo(415);
    }

    @Test
    @DisplayName("POST /api/scores - Debe rechazar archivo >10MB con HTTP 413")
    void uploadScore_WithFileTooLarge_ShouldReturn413() {
        // GIVEN
        when(uploadScoreUseCase.uploadScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new FileSizeExceededException("The file exceeds the maximum allowed size")));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithLargeFile().build()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE)
                .expectBody()
                .jsonPath("$.message").isEqualTo("The file exceeds the maximum allowed size")
                .jsonPath("$.status").isEqualTo(413);
    }
}