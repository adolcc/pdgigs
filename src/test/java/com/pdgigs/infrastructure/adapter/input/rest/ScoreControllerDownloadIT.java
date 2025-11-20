package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.config.TestWebClientConfig;
import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.helper.PdfContentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestWebClientConfig.class)
@DisplayName("Controller: Descarga de partituras PDF")
class ScoreControllerDownloadIT {

    private static final String SCORE_ID = "507f1f77bcf86cd799439011";
    private static final String NON_EXISTENT_ID = "507f1f77bcf86cd799439099";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GetScorePdfUseCase getScorePdfUseCase;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/scores/{id}/download - PDF existe → 200 con contenido")
    void downloadScorePdf_PdfExists_Returns200WithContent() {
        // GIVEN
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();

        when(getScorePdfUseCase.getPdfContentById(SCORE_ID))
                .thenReturn(Mono.just(pdfContent));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", SCORE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Disposition",
                        "form-data; name=\"attachment\"; filename=\"score-" + SCORE_ID + ".pdf\"")
                .expectHeader().valueEquals("Content-Length", String.valueOf(pdfContent.length))
                .expectBody(byte[].class)
                .isEqualTo(pdfContent);

        verify(getScorePdfUseCase).getPdfContentById(SCORE_ID);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/scores/{id}/download - Partitura no existe → 404")
    void downloadScorePdf_ScoreNotFound_Returns404() {
        // GIVEN
        when(getScorePdfUseCase.getPdfContentById(NON_EXISTENT_ID))
                .thenReturn(Mono.error(ResourceNotFoundException.score(NON_EXISTENT_ID)));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", NON_EXISTENT_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").value(msg ->
                        msg.toString().contains("Score not found with ID: " + NON_EXISTENT_ID))
                .jsonPath("$.errorCode").isEqualTo("RESOURCE_NOT_FOUND");

        verify(getScorePdfUseCase).getPdfContentById(NON_EXISTENT_ID);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/scores/{id}/download - PDF grande (10MB) → 200")
    void downloadScorePdf_LargePdf_Returns200() {
        // GIVEN
        byte[] largePdfContent = PdfContentFactory.createValidPdfContent(10240); // 10 MB

        when(getScorePdfUseCase.getPdfContentById(SCORE_ID))
                .thenReturn(Mono.just(largePdfContent));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", SCORE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Length", String.valueOf(largePdfContent.length))
                .expectBody(byte[].class)
                .value(bytes -> {
                    assertThat(bytes).isNotNull();
                    assertThat(bytes).hasSize(largePdfContent.length);
                    assertThat(bytes[0]).isEqualTo((byte) '%'); // PDF magic number
                    assertThat(bytes[1]).isEqualTo((byte) 'P');
                    assertThat(bytes[2]).isEqualTo((byte) 'D');
                    assertThat(bytes[3]).isEqualTo((byte) 'F');
                });

        verify(getScorePdfUseCase).getPdfContentById(SCORE_ID);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/scores/{id}/download - PDF vacío → 200 con 0 bytes")
    void downloadScorePdf_EmptyPdf_Returns200WithZeroBytes() {
        // GIVEN
        byte[] emptyPdfContent = new byte[0];

        when(getScorePdfUseCase.getPdfContentById(SCORE_ID))
                .thenReturn(Mono.just(emptyPdfContent));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", SCORE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Length", "0")
                .expectBody(byte[].class)
                .isEqualTo(emptyPdfContent);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/scores/{id}/download - Error interno → 500")
    void downloadScorePdf_InternalError_Returns500() {
        // GIVEN
        when(getScorePdfUseCase.getPdfContentById(SCORE_ID))
                .thenReturn(Mono.error(new RuntimeException("Database connection lost")));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", SCORE_ID)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(getScorePdfUseCase).getPdfContentById(SCORE_ID);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/scores/{id}/download - Headers correctos para attachment")
    void downloadScorePdf_ValidRequest_ReturnsCorrectHeaders() {
        // GIVEN
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();

        when(getScorePdfUseCase.getPdfContentById(SCORE_ID))
                .thenReturn(Mono.just(pdfContent));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", SCORE_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Content-Disposition")
                .expectHeader().exists("Content-Length")
                .expectHeader().contentType(MediaType.APPLICATION_PDF);
    }

    @Test
    @DisplayName("GET /api/scores/{id}/download - Sin autenticación → 401")
    void downloadScorePdf_NotAuthenticated_Returns401() {
        // WHEN & THEN (sin @WithMockUser)
        webTestClient.get()
                .uri("/api/scores/{id}/download", SCORE_ID)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}