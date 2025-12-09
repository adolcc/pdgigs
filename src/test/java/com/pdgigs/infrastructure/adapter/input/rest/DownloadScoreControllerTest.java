package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.application.service.ScoreDownloadService;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadScoreControllerTest {

    @Mock
    private GetScorePdfUseCase getScorePdfUseCase;

    @Mock
    private GetScoreMetadataUseCase getScoreMetadataUseCase;

    private ScoreControllerDownload controller;
    private WebTestClient webTestClient;

    private final byte[] pdfBytes = "PDF-BYTES-EXAMPLE".getBytes();

    @BeforeEach
    void setUp() {
        ScoreDownloadService scoreDownloadService = new ScoreDownloadService(getScoreMetadataUseCase, getScorePdfUseCase);
        controller = new ScoreControllerDownload(getScoreMetadataUseCase, scoreDownloadService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void downloadScorePdf_whenExists_returnsPdfAndHeaders() {
        Score metadata = new Score("P-42",
                "Symphony No.5",
                "Ludwig van Beethoven",
                "Classical",
                "stored-file.pdf",
                LocalDateTime.now());

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

        when(getScoreMetadataUseCase.findById(eq("P-42"))).thenReturn(Mono.just(metadata));
        when(getScorePdfUseCase.getPdf(eq("P-42"))).thenReturn(Mono.just(resource));

        webTestClient.get()
                .uri("/api/scores/P-42/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_DISPOSITION, ".*stored-file\\.pdf.*")
                .expectBody(byte[].class).isEqualTo(pdfBytes);

        verify(getScoreMetadataUseCase, times(1)).findById("P-42");
        verify(getScorePdfUseCase, times(1)).getPdf("P-42");
    }

    @Test
    void downloadScorePdf_whenContentLengthThrows_returnsPdfAndHeaders() {
        Score metadata = new Score("P-43",
                "Another",
                "Composer",
                "Style",
                "file-with-bad-length.pdf",
                LocalDateTime.now());

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

        when(getScoreMetadataUseCase.findById(eq("P-43"))).thenReturn(Mono.just(metadata));
        when(getScorePdfUseCase.getPdf(eq("P-43"))).thenReturn(Mono.just(resource));

        webTestClient.get()
                .uri("/api/scores/P-43/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_DISPOSITION, ".*file-with-bad-length\\.pdf.*")
                .expectBody(byte[].class).isEqualTo(pdfBytes);

        verify(getScoreMetadataUseCase, times(1)).findById("P-43");
        verify(getScorePdfUseCase, times(1)).getPdf("P-43");
    }

    @Test
    void downloadScorePdf_whenMetadataMissing_returnsNotFound() {
        when(getScoreMetadataUseCase.findById(eq("P-99"))).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/scores/P-99/pdf")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isNotFound();

        verify(getScoreMetadataUseCase, times(1)).findById("P-99");
        verify(getScorePdfUseCase, never()).getPdf("P-99");
    }
}