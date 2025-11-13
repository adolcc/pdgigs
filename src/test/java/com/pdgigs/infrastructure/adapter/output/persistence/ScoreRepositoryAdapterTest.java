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
        Score score = new Score(
                null,
                "Concierto Nº 5",
                "Mozart",
                "Clásico",
                validPdfContent,
                (long) validPdfContent.length
        );

        // WHEN
        StepVerifier.create(scoreRepositoryAdapter.save(score))
                // THEN
                .assertNext(savedScore -> {
                    assertThat(savedScore.id()).isNotNull();
                    assertThat(savedScore.title()).isEqualTo("Concierto Nº 5");
                    assertThat(savedScore.author()).isEqualTo("Mozart");
                    assertThat(savedScore.musicalStyle()).isEqualTo("Clásico");
                    assertThat(savedScore.pdfContent()).isEqualTo(validPdfContent);
                    assertThat(savedScore.fileSize()).isEqualTo((long) validPdfContent.length);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe generar ID automáticamente al guardar")
    void save_ShouldGenerateIdAutomatically() {
        // GIVEN
        Score score = new Score(
                null,
                "Test Score",
                "",
                "",
                validPdfContent,
                (long) validPdfContent.length
        );

        // WHEN & THEN
        StepVerifier.create(scoreRepositoryAdapter.save(score))
                .assertNext(savedScore ->
                        assertThat(savedScore.id()).isNotEmpty())
                .verifyComplete();
    }
}