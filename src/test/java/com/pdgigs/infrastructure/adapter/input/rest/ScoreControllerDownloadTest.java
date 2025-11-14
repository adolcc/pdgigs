package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import com.pdgigs.domain.exception.ScoreNotFoundException;
import com.pdgigs.infrastructure.adapter.input.rest.helper.PdfContentFactory;
import com.pdgigs.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(ScoreControllerDownload.class)
@Import(SecurityConfig.class)
@DisplayName("Controller: Descarga de partituras PDF")
class ScoreControllerDownloadTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private GetScorePdfUseCase getScorePdfUseCase;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .configureClient()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(20 * 1024 * 1024))
                .build();
    }

    @Test
    @DisplayName("GET /api/scores/{id}/download - Debe descargar PDF exitosamente")
    void downloadScorePdf_WhenExists_ShouldReturn200WithPdf() {
        // GIVEN
        String scoreId = "674b8e1234567890abcdef12";
        byte[] pdfContent = PdfContentFactory.createValidPdfContent();

        when(getScorePdfUseCase.getPdfContentById(scoreId))
                .thenReturn(Mono.just(pdfContent));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Disposition", "form-data; name=\"attachment\"; filename=\"score-" + scoreId + ".pdf\"")
                .expectHeader().valueEquals("Content-Length", String.valueOf(pdfContent.length))
                .expectBody(byte[].class)
                .isEqualTo(pdfContent);
    }

    @Test
    @DisplayName("GET /api/scores/{id}/download - Debe retornar 404 cuando no existe")
    void downloadScorePdf_WhenNotExists_ShouldReturn404() {
        // GIVEN
        String scoreId = "non-existent-id";
        when(getScorePdfUseCase.getPdfContentById(scoreId))
                .thenReturn(Mono.error(new ScoreNotFoundException("Score not found with ID: " + scoreId)));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").value(msg -> msg.toString().contains("Score not found"));
    }

    @Test
    @DisplayName("GET /api/scores/{id}/download - Debe manejar PDFs grandes (hasta 10MB) correctamente")
    void downloadScorePdf_WithLargePdf_ShouldReturnCorrectly() {
        // GIVEN
        String scoreId = "large-score-id";
        byte[] largePdfContent = PdfContentFactory.createValidPdfContent(10240); // 10 MB PDF

        when(getScorePdfUseCase.getPdfContentById(scoreId))
                .thenReturn(Mono.just(largePdfContent));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Length", String.valueOf(largePdfContent.length))
                .expectBody(byte[].class)
                .value(bytes -> {
                    assert bytes != null;
                    assert bytes.length == largePdfContent.length;
                });
    }

    @Test
    @DisplayName("GET /api/scores/{id}/download - Debe manejar PDF vacío")
    void downloadScorePdf_WithEmptyPdf_ShouldReturnEmpty() {
        // GIVEN
        String scoreId = "empty-score-id";
        byte[] emptyPdfContent = new byte[0];

        when(getScorePdfUseCase.getPdfContentById(scoreId))
                .thenReturn(Mono.just(emptyPdfContent));

        // WHEN & THEN
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Length", "0")
                .expectBody(byte[].class)
                .isEqualTo(emptyPdfContent);
    }
}