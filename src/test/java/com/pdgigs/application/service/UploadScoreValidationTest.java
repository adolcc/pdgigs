package com.pdgigs.application.service;

import com.pdgigs.domain.exception.FileSizeExceededException;
import com.pdgigs.domain.exception.InvalidFileFormatException;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("Criterios 4 y 5: Validaciones de formato y tamaño")
class UploadScoreValidationTest {

    @Mock
    private ScoreRepository scoreRepository;
    private UploadScoreService uploadScoreService;

    private byte[] validPdfContent;

    @BeforeEach
    void setUp() {
        uploadScoreService = new UploadScoreService(scoreRepository);
        String pdfHeader = "%PDF-1.4\nfake-pdf-content";
        validPdfContent = pdfHeader.getBytes();
    }

    @Test
    @DisplayName("Debe rechazar archivo que no es PDF")
    void uploadScore_WithNonPdfFile_ShouldThrowInvalidFileFormatException() {
        // GIVEN
        byte[] nonPdfContent = "not-a-pdf".getBytes();

        // WHEN & THEN
        StepVerifier.create(uploadScoreService.uploadScore(nonPdfContent, "", "", ""))
                .expectError(InvalidFileFormatException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe rechazar archivo que excede 10MB")
    void uploadScore_WithFileSizeExceeding10MB_ShouldThrowFileSizeExceededException() {
        // GIVEN
        byte[] largePdfContent = new byte[11 * 1024 * 1024]; // 11 MB
        largePdfContent[0] = '%';
        largePdfContent[1] = 'P';
        largePdfContent[2] = 'D';
        largePdfContent[3] = 'F';

        // WHEN & THEN
        StepVerifier.create(uploadScoreService.uploadScore(largePdfContent, "", "", ""))
                .expectError(FileSizeExceededException.class)
                .verify();
    }
}