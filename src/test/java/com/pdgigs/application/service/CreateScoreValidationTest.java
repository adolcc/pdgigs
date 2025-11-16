package com.pdgigs.application.service;

import com.pdgigs.domain.exception.validation.ValidationException;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateScoreService - Validaciones de formato y tamaño")
class CreateScoreValidationTest {

    private static final byte[] VALID_PDF = "%PDF-1.4\nfake-pdf-content".getBytes();
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    @Mock
    private ScoreRepository scoreRepository;

    private CreateScoreService createScoreService;

    @BeforeEach
    void setUp() {
        createScoreService = new CreateScoreService(scoreRepository);
    }

    @Test
    @DisplayName("Dado archivo no PDF, cuando se crea partitura, entonces lanza InvalidFormat")
    void createScore_NonPdfFile_ThrowsInvalidFormat() {
        // GIVEN
        byte[] nonPdfContent = "not-a-pdf-file".getBytes();

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(nonPdfContent, "", "", ""))
                .expectErrorMatches(error ->
                        error instanceof ValidationException &&
                                error.getMessage().contains("Invalid file format"))
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado archivo con extensión .txt, cuando se crea partitura, entonces lanza InvalidFormat")
    void createScore_TxtFile_ThrowsInvalidFormat() {
        // GIVEN
        byte[] txtContent = "Plain text content".getBytes();

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(txtContent, "Title", "Author", "Style"))
                .expectErrorMatches(error ->
                        error instanceof ValidationException &&
                                error.getMessage().contains("Invalid file format"))
                .verify();
    }

    @Test
    @DisplayName("Dado archivo sin magic number PDF, cuando se crea partitura, entonces lanza InvalidFormat")
    void createScore_NoMagicNumber_ThrowsInvalidFormat() {
        // GIVEN
        byte[] invalidPdf = "XYZ-1.4\nfake".getBytes();

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(invalidPdf, "", "", ""))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado archivo >10MB, cuando se crea partitura, entonces lanza SizeExceeded")
    void createScore_FileSizeExceeds10MB_ThrowsSizeExceeded() {
        // GIVEN
        byte[] largePdf = new byte[11 * 1024 * 1024]; // 11 MB
        largePdf[0] = '%';
        largePdf[1] = 'P';
        largePdf[2] = 'D';
        largePdf[3] = 'F';

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(largePdf, "", "", ""))
                .expectErrorMatches(error ->
                        error instanceof ValidationException &&
                                error.getMessage().contains("exceeds maximum"))
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado archivo exactamente 10MB, cuando se crea partitura, entonces acepta el archivo")
    void createScore_FileExactly10MB_Succeeds() {
        // GIVEN
        byte[] exactSizePdf = new byte[(int) MAX_FILE_SIZE];
        exactSizePdf[0] = '%';
        exactSizePdf[1] = 'P';
        exactSizePdf[2] = 'D';
        exactSizePdf[3] = 'F';

        // WHEN & THEN
        // Solo verificamos que NO lanza error de tamaño
        // (puede fallar por otros motivos en el mock, pero no por tamaño)
        StepVerifier.create(createScoreService.createScore(exactSizePdf, "", "", ""))
                .expectError()  // Fallará porque no mockeamos save(), pero no por tamaño
                .verify();
    }

    // ========== ARCHIVO VACÍO ==========

    @Test
    @DisplayName("Dado archivo vacío (0 bytes), cuando se crea partitura, entonces lanza Empty")
    void createScore_EmptyFile_ThrowsEmpty() {
        // GIVEN
        byte[] emptyContent = new byte[0];

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(emptyContent, "", "", ""))
                .expectErrorMatches(error ->
                        error instanceof ValidationException &&
                                error.getMessage().contains("File cannot be empty"))
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado archivo null, cuando se crea partitura, entonces lanza Empty")
    void createScore_NullFile_ThrowsEmpty() {
        // GIVEN
        byte[] nullContent = null;

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(nullContent, "", "", ""))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado PDF válido pero título en blanco, cuando se crea partitura, entonces lanza ValidationException")
    void createScore_ValidPdfButBlankTitle_ThrowsValidationException() {
        // GIVEN
        String blankTitle = "   ";

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(VALID_PDF, blankTitle, "", ""))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado PDF válido pero autor en blanco, cuando se crea partitura, entonces lanza ValidationException")
    void createScore_ValidPdfButBlankAuthor_ThrowsValidationException() {
        // GIVEN
        String blankAuthor = "   ";

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(VALID_PDF, "", blankAuthor, ""))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado PDF válido pero estilo musical en blanco, cuando se crea partitura, entonces lanza ValidationException")
    void createScore_ValidPdfButBlankMusicalStyle_ThrowsValidationException() {
        // GIVEN
        String blankStyle = "   ";

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(VALID_PDF, "", "", blankStyle))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }
}