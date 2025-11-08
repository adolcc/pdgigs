package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.domain.model.Score;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
@Import(ScoreRepositoryAdapter.class)
@DisplayName("Criterio 6: Almacenamiento en MongoDB")
class ScoreRepositoryAdapterTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ScoreRepositoryAdapter scoreRepositoryAdapter;

    @Autowired
    private MongoScoreRepository mongoScoreRepository;

    private byte[] validPdfContent;

    @BeforeEach
    void setUp() {
        mongoScoreRepository.deleteAll().block();
        String pdfHeader = "%PDF-1.4\nfake-pdf-content";
        validPdfContent = pdfHeader.getBytes();
    }

    @Test
    @DisplayName("Debe guardar partitura en MongoDB correctamente")
    void save_ShouldPersistScoreInMongoDB() {
        // GIVEN
        Score score = Score.builder()
                .title("Concierto Nº 5")
                .author("Mozart")
                .musicalStyle("Clásico")
                .pdfContent(validPdfContent)
                .fileSize((long) validPdfContent.length)
                .build();

        // WHEN
        StepVerifier.create(scoreRepositoryAdapter.save(score))
                // THEN
                .assertNext(savedScore -> {
                    assertThat(savedScore.getId()).isNotNull();
                    assertThat(savedScore.getTitle()).isEqualTo("Concierto Nº 5");
                    assertThat(savedScore.getAuthor()).isEqualTo("Mozart");
                    assertThat(savedScore.getMusicalStyle()).isEqualTo("Clásico");
                    assertThat(savedScore.getPdfContent()).isEqualTo(validPdfContent);
                    assertThat(savedScore.getFileSize()).isEqualTo(validPdfContent.length);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe generar ID automáticamente al guardar")
    void save_ShouldGenerateIdAutomatically() {
        // GIVEN
        Score score = Score.builder()
                .title("Test Score")
                .author("")
                .musicalStyle("")
                .pdfContent(validPdfContent)
                .fileSize((long) validPdfContent.length)
                .build();

        // WHEN & THEN
        StepVerifier.create(scoreRepositoryAdapter.save(score))
                .assertNext(savedScore ->
                        assertThat(savedScore.getId()).isNotEmpty())
                .verifyComplete();
    }
}