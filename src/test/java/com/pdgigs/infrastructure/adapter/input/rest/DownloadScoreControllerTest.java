package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.dto.DownloadableScore;
import com.pdgigs.application.service.ScoreDownloadService;
import com.pdgigs.domain.model.Score;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadScoreControllerTest {

    @Mock
    private ScoreDownloadService scoreDownloadService;

    private ScoreDownloadController controller;
    private WebTestClient webTestClient;

    private final byte[] pdfBytes = "PDF-BYTES-EXAMPLE".getBytes();

    @BeforeEach
    void setUp() {
        controller = new ScoreDownloadController(scoreDownloadService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void downloadScorePdf_whenExists_returnsPdfAndHeaders() {
        Score metadata = new Score(
                "P-42",
                "Symphony No.5",
                "Ludwig van Beethoven",
                "Classical",
                "stored-file.pdf",
                "uploader@example.com",
                LocalDateTime.now()
        );

        Resource resource = new ByteArrayResource(pdfBytes) {
            @Override
            public java.nio.channels.ReadableByteChannel readableChannel() {
                try {
                    return Channels.newChannel(getInputStream());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // prepare downloadable result that the service returns
        DownloadableScore downloadable = new DownloadableScore(resource, "stored-file.pdf", Optional.of((long) pdfBytes.length));

        when(scoreDownloadService.prepareDownload(eq("P-42"))).thenReturn(Mono.just(downloadable));

        webTestClient.get()
                .uri("/api/scores/P-42/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_DISPOSITION, ".*stored-file\\.pdf.*")
                .expectBody(byte[].class).isEqualTo(pdfBytes);

        verify(scoreDownloadService, times(1)).prepareDownload("P-42");
        verifyNoMoreInteractions(scoreDownloadService);
    }

    @Test
    void downloadScorePdf_whenContentLengthThrows_returnsPdfAndHeaders() {
        Score metadata = new Score(
                "P-43",
                "Another",
                "Composer",
                "Style",
                "file-with-bad-length.pdf",
                "uploader@example.com",
                LocalDateTime.now()
        );

        Resource resource = new ByteArrayResource(pdfBytes) {
            @Override
            public long contentLength() {
                return -1L;
            }

            @Override
            public java.nio.channels.ReadableByteChannel readableChannel() {
                return Channels.newChannel(new ByteArrayInputStream(pdfBytes));
            }
        };

        DownloadableScore downloadable = new DownloadableScore(resource, "file-with-bad-length.pdf", Optional.empty());

        when(scoreDownloadService.prepareDownload(eq("P-43"))).thenReturn(Mono.just(downloadable));

        webTestClient.get()
                .uri("/api/scores/P-43/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_DISPOSITION, ".*file-with-bad-length\\.pdf.*")
                .expectBody(byte[].class).isEqualTo(pdfBytes);

        verify(scoreDownloadService, times(1)).prepareDownload("P-43");
        verifyNoMoreInteractions(scoreDownloadService);
    }

    @Test
    void downloadScorePdf_whenNotFound_returnsNotFound() {
        when(scoreDownloadService.prepareDownload(eq("P-99"))).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/scores/P-99/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isNotFound();

        verify(scoreDownloadService, times(1)).prepareDownload("P-99");
        verifyNoMoreInteractions(scoreDownloadService);
    }
}