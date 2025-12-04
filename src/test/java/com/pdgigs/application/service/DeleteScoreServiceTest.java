package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteScoreServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private DeleteScoreService deleteScoreService;

    @Captor
    private ArgumentCaptor<String> filenameCaptor;

    private Score existing;

    @BeforeEach
    void setUp() {
        existing = new Score(
                "P-55",
                "Título",
                "Autor",
                "Estilo",
                "stored-file.pdf",
                LocalDateTime.of(2020,1,1,0,0)
        );
    }

    @Test
    void given_existing_score_when_delete_then_remove_file_and_entity() {
        when(scoreRepository.findById(eq("P-55"))).thenReturn(Mono.just(existing));
        when(fileStoragePort.delete(eq("stored-file.pdf"))).thenReturn(Mono.empty());
        when(scoreRepository.deleteById(eq("P-55"))).thenReturn(Mono.empty());

        StepVerifier.create(deleteScoreService.deleteById("P-55"))
                .verifyComplete();

        verify(fileStoragePort, times(1)).delete(filenameCaptor.capture());
        verify(scoreRepository, times(1)).deleteById("P-55");

        assertThat(filenameCaptor.getValue()).isEqualTo("stored-file.pdf");
    }

    @Test
    void given_nonexistent_score_when_delete_then_throw_not_found() {
        when(scoreRepository.findById(eq("P-99"))).thenReturn(Mono.empty());

        StepVerifier.create(deleteScoreService.deleteById("P-99"))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().contains("P-99"))
                .verify();

        verify(fileStoragePort, never()).delete(anyString());
        verify(scoreRepository, never()).deleteById(anyString());
    }
}