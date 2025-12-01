package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DisplayName("ScoreController - Download Score Success Tests")
class ScoreControllerDownloadSuccessIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GetScorePdfUseCase getScorePdfUseCase;

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should download PDF successfully and return 200 with correct headers")
    void downloadScorePdf_ValidId_Returns200WithPdf() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";
        byte[] pdfContent = "%PDF-1.4\nTest PDF Content".getBytes();

        when(getScorePdfUseCase.getPdfContentById(eq(scoreId)))
                .thenReturn(Mono.just(pdfContent));

        // When & Then
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Disposition", "form-data; name=\"attachment\"; filename=\"score-" + scoreId + ".pdf\"")
                .expectHeader().valueEquals("Content-Length", String.valueOf(pdfContent.length))
                .expectBody(byte[].class).isEqualTo(pdfContent);

        verify(getScorePdfUseCase).getPdfContentById(scoreId);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should handle large PDF files successfully")
    void downloadScorePdf_LargePdf_Returns200() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";
        byte[] largePdfContent = createLargePdfContent(2 * 1024 * 1024); // 2MB

        when(getScorePdfUseCase.getPdfContentById(eq(scoreId)))
                .thenReturn(Mono.just(largePdfContent));

        // When & Then
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Length", String.valueOf(largePdfContent.length))
                .expectBody(byte[].class).isEqualTo(largePdfContent);

        verify(getScorePdfUseCase).getPdfContentById(scoreId);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    @DisplayName("Should handle empty PDF content")
    void downloadScorePdf_EmptyPdf_Returns200WithZeroBytes() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";
        byte[] emptyContent = new byte[0];

        when(getScorePdfUseCase.getPdfContentById(eq(scoreId)))
                .thenReturn(Mono.just(emptyContent));

        // When & Then
        webTestClient.get()
                .uri("/api/scores/{id}/download", scoreId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueEquals("Content-Length", "0")
                .expectBody(byte[].class).isEqualTo(emptyContent);

        verify(getScorePdfUseCase).getPdfContentById(scoreId);
    }

    private byte[] createLargePdfContent(int size) {
        byte[] content = new byte[size];
        System.arraycopy("%PDF-1.4".getBytes(), 0, content, 0, 7);
        for (int i = 7; i < size; i++) {
            content[i] = (byte) 'x';
        }
        return content;
    }
}