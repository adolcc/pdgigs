package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.exception.validation.FileValidationError;
import com.pdgigs.domain.exception.validation.ValidationException;
import com.pdgigs.domain.port.input.CreateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.helper.MultipartRequestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@DisplayName("Controller: Validaciones de formato y tamaño")
class ScoreControllerValidationIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateScoreUseCase createScoreUseCase;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/scores - Archivo no PDF → 415")
    void uploadScore_InvalidFormat_Returns415() {
        // GIVEN
        ValidationException validationException = new FileValidationError.InvalidFormat().toException();

        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(validationException));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithInvalidFormat().build()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(415)
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/scores - Archivo >10MB → 413")
    void uploadScore_FileTooLarge_Returns413() {
        // GIVEN
        long actualSize = 11 * 1024 * 1024;
        long maxSize = 10 * 1024 * 1024;
        ValidationException validationException = new FileValidationError.SizeExceeded(actualSize, maxSize).toException();

        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(validationException));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithLargeFile().build()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(413)
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/scores - Archivo vacío → 400")
    void uploadScore_EmptyFile_Returns400() {
        // GIVEN
        ValidationException validationException = new FileValidationError.Empty().toException();

        when(createScoreUseCase.createScore(any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(validationException));

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createWithEmptyFile().build()))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("POST /api/scores - Sin autenticación → 401")
    void uploadScore_NotAuthenticated_Returns401() {
        // WHEN & THEN (sin @WithMockUser)
        webTestClient.post()
                .uri("/api/scores")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(
                        MultipartRequestFactory.createValidRequest().build()))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}