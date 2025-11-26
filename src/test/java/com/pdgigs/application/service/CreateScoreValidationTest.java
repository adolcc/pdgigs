package com.pdgigs.application.service;

import com.pdgigs.domain.exception.validation.ValidationException;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.domain.port.output.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateScoreService - Validaciones")
class CreateScoreValidationTest {

    private static final byte[] VALID_PDF = "%PDF-1.4\nfake-pdf-content".getBytes();
    private static final String USER_EMAIL = "test@example.com";
    private static final User TEST_USER = new User("user123", USER_EMAIL, "Test User", "password", User.ROLE_USER, null, null);

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private UserRepository userRepository;

    private CreateScoreService createScoreService;

    @BeforeEach
    void setUp() {
        createScoreService = new CreateScoreService(scoreRepository, userRepository);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Mono.just(TEST_USER));
    }

    @Test
    @DisplayName("Archivo no PDF lanza InvalidFormat")
    void createScore_NonPdfFile_ThrowsInvalidFormat() {
        // GIVEN
        byte[] nonPdfContent = "not-a-pdf-file".getBytes();

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(nonPdfContent, "Title", "Author", "Style", USER_EMAIL))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Archivo >10MB lanza SizeExceeded")
    void createScore_FileSizeExceeds10MB_ThrowsSizeExceeded() {
        // GIVEN
        byte[] largePdf = createPdfBytes(11 * 1024 * 1024); // 11 MB

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(largePdf, "Title", "Author", "Style", USER_EMAIL))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Archivo vacío lanza Empty")
    void createScore_EmptyFile_ThrowsEmpty() {
        // GIVEN
        byte[] emptyContent = new byte[0];

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(emptyContent, "Title", "Author", "Style", USER_EMAIL))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Archivo null lanza Empty")
    void createScore_NullFile_ThrowsEmpty() {
        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(null, "Title", "Author", "Style", USER_EMAIL))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Título en blanco lanza ValidationException")
    void createScore_BlankTitle_ThrowsValidationException() {
        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(VALID_PDF, "   ", "Author", "Style", USER_EMAIL))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Autor en blanco lanza ValidationException")
    void createScore_BlankAuthor_ThrowsValidationException() {
        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(VALID_PDF, "Title", "   ", "Style", USER_EMAIL))
                .expectError(ValidationException.class)
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Usuario no encontrado lanza error")
    void createScore_UserNotFound_ThrowsError() {
        // GIVEN
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Mono.empty());

        // WHEN & THEN
        StepVerifier.create(createScoreService.createScore(VALID_PDF, "Title", "Author", "Style", "unknown@example.com"))
                .expectErrorMatches(error -> error.getMessage().contains("User not found"))
                .verify();

        verify(scoreRepository, never()).save(any());
    }

    // Método helper para crear PDF bytes
    private byte[] createPdfBytes(int size) {
        byte[] pdf = new byte[size];
        pdf[0] = '%';
        pdf[1] = 'P';
        pdf[2] = 'D';
        pdf[3] = 'F';
        return pdf;
    }
}