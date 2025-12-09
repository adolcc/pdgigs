package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private FilePart filePart;

    @InjectMocks
    private UploadScoreService uploadScoreService;

    @BeforeEach
    void setUp() {

        lenient().when(filePart.filename()).thenReturn("file.pdf");
        lenient().when(fileStoragePort.store(eq(filePart), anyString())).thenReturn(Mono.just("stored-file.pdf"));

        lenient().when(scoreRepository.save(any(Score.class))).thenAnswer(invocation -> {
            Score s = invocation.getArgument(0);
            Score withId = new Score(
                    s.id() == null ? "generated-id" : s.id(),
                    s.title(),
                    s.author(),
                    s.musicStyle(),
                    s.filename(),
                    s.createdAt() == null ? LocalDateTime.now() : s.createdAt()
            );
            return Mono.just(withId);
        });
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> partialFieldsProvider() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("title", "Concierto Nº 5"),
                org.junit.jupiter.params.provider.Arguments.of("author", "Ludwig van Beethoven"),
                org.junit.jupiter.params.provider.Arguments.of("musicStyle", "Clásica")
        );
    }

    @ParameterizedTest
    @MethodSource("partialFieldsProvider")
    void given_partial_field_when_upload_then_save_provided_and_others_empty(String fieldName, String value) {

        String title = "title".equals(fieldName) ? value : null;
        String author = "author".equals(fieldName) ? value : null;
        String musicStyle = "musicStyle".equals(fieldName) ? value : null;

        Mono<Score> result = uploadScoreService.upload(filePart, title, author, musicStyle, null);

        StepVerifier.create(result)
                .assertNext(score -> {
                    if ("title".equals(fieldName)) {
                        assertThat(score.title()).isEqualTo(value);
                        assertThat(score.author()).isEqualTo("");
                        assertThat(score.musicStyle()).isEqualTo("");
                    } else if ("author".equals(fieldName)) {
                        assertThat(score.title()).isEqualTo("");
                        assertThat(score.author()).isEqualTo(value);
                        assertThat(score.musicStyle()).isEqualTo("");
                    } else {
                        assertThat(score.title()).isEqualTo("");
                        assertThat(score.author()).isEqualTo("");
                        assertThat(score.musicStyle()).isEqualTo(value);
                    }
                    assertThat(score.filename()).isEqualTo("stored-file.pdf");
                })
                .verifyComplete();

        verify(fileStoragePort, times(1)).store(eq(filePart), eq("file.pdf"));
        verify(scoreRepository, times(1)).save(any(Score.class));
    }

    @Test
    void given_full_metadata_when_upload_then_save_all_fields() {

        String title = "Concierto Completo";
        String author = "Autor Ejemplo";
        String musicStyle = "Jazz";

        Mono<Score> result = uploadScoreService.upload(filePart, title, author, musicStyle, null);

        StepVerifier.create(result)
                .assertNext(score -> {
                    assertThat(score.title()).isEqualTo(title);
                    assertThat(score.author()).isEqualTo(author);
                    assertThat(score.musicStyle()).isEqualTo(musicStyle);
                    assertThat(score.filename()).isEqualTo("stored-file.pdf");
                })
                .verifyComplete();

        verify(fileStoragePort, times(1)).store(eq(filePart), eq("file.pdf"));
        verify(scoreRepository, times(1)).save(any(Score.class));
    }

    @Test
    void given_non_pdf_file_when_upload_then_error_file_format_not_allowed() {

        when(filePart.filename()).thenReturn("not-a-pdf.txt");

        Mono<Score> result = uploadScoreService.upload(filePart, null, null, null, null);

        StepVerifier.create(result)
                .expectErrorMessage("Validation failed for 'file': File format not allowed")
                .verify();

        verify(fileStoragePort, never()).store(any(), anyString());
        verify(scoreRepository, never()).save(any());
    }

    @Test
    void given_pdf_exceeding_max_size_when_upload_then_error_file_too_large() {

        when(filePart.filename()).thenReturn("huge.pdf");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(11L * 1024L * 1024L);
        when(filePart.headers()).thenReturn(headers);

        Mono<Score> result = uploadScoreService.upload(filePart, null, null, null, null);

        StepVerifier.create(result)
                .expectErrorMessage("Validation failed for 'file': The file exceeds the maximum allowed size")
                .verify();

        verify(fileStoragePort, never()).store(any(), anyString());
        verify(scoreRepository, never()).save(any());
    }
}