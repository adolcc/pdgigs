package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GetScorePdfServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private GetScorePdfService getScorePdfService;

    private Score sampleScore;
    private static final byte[] PDF_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF

    @BeforeEach
    void setUp() {
        sampleScore = new Score(
                "P-42",
                "Symphony No.5",
                "Ludwig van Beethoven",
                "Classical",
                "stored-file.pdf",
                LocalDateTime.now()
        );
    }

    @Test
    void given_existing_score_when_getPdf_then_return_resource() {
        when(scoreRepository.findById(eq("P-42"))).thenReturn(Mono.just(sampleScore));

        when(fileStoragePort.download(eq("stored-file.pdf")))
                .thenAnswer(invocation -> Mono.just(new ByteArrayResource(PDF_BYTES)));

        StepVerifier.create(getScorePdfService.getPdf("P-42"))
                .assertNext(resource -> {
                    assertThat(resource).isInstanceOf(Resource.class);
                    try {
                        byte[] body = resource.getInputStream().readAllBytes();
                        assertThat(body).isEqualTo(PDF_BYTES);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void given_missing_score_when_getPdf_then_return_not_found() {
        when(scoreRepository.findById(eq("P-99"))).thenReturn(Mono.empty());

        StepVerifier.create(getScorePdfService.getPdf("P-99"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResourceNotFoundException &&
                                throwable.getMessage().contains("P-99")
                )
                .verify();
    }

    @Test
    void given_score_without_storageid_when_getPdf_then_return_not_found() {
        Score sWithoutFile = new Score(
                "P-43",
                "Untitled",
                "Unknown",
                "",
                "",
                LocalDateTime.now()
        );
        when(scoreRepository.findById(eq("P-43"))).thenReturn(Mono.just(sWithoutFile));

        StepVerifier.create(getScorePdfService.getPdf("P-43"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResourceNotFoundException &&
                                throwable.getMessage().contains("P-43")
                )
                .verify();
    }
}